/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Authority
import com.mobilabsolutions.payment.data.MerchantUser
import com.mobilabsolutions.payment.data.PasswordResetToken
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.data.repository.PasswordResetTokenRepository
import com.mobilabsolutions.payment.model.request.MerchantUserEditPasswordRequestModel
import com.mobilabsolutions.payment.model.request.MerchantUserEditRequestModel
import com.mobilabsolutions.payment.model.request.MerchantUserRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.sendgrid.Content
import com.sendgrid.Email
import com.sendgrid.Mail
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.servlet.http.HttpServletRequest

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
class UserDetailsServiceImpl(
    private val merchantUserRepository: MerchantUserRepository,
    private val authorityRepository: AuthorityRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val merchantRepository: MerchantRepository,
    private val sendGrid: SendGrid,
    @Qualifier("userPasswordEncoder") private val userPasswordEncoder: PasswordEncoder
) : UserDetailsService {

    @Value("\${initial.data.loader.adminUsername}")
    lateinit var adminUsername: String

    @Value("\${payment.mail.address:}")
    private lateinit var paymentEmail: String

    /**
     * Find merchant user by id and wrap the merchant user with extra information for authentication
     *
     * @param email Merchant user email
     * @return Enhanced merchant user
     */
    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        return merchantUserRepository.findByEmail(email)?.toUserDetails()
            ?: throw ApiError.ofErrorCode(ApiErrorCode.AUTHENTICATION_ERROR).asException()
    }

    /**
     * Update the merchant user by given userId
     *
     * @param userId Merchant userId
     * @param principal Currently authenticated user
     * @param merchantUserModel Merchant user model
     */
    @Transactional
    fun updateMerchantUser(userId: String, principal: String, merchantUserModel: MerchantUserEditRequestModel) {
        if (principal != adminUsername && principal != userId) throw ApiError.ofErrorCode(ApiErrorCode.INSUFFICIENT_RIGHTS).asException()
        merchantUserRepository.updateMerchantUser(
            userId,
            merchantUserModel.firstname,
            merchantUserModel.lastname,
            merchantUserModel.locale
        )
    }

    /**
     * Change the password of the merchant user by given userId
     *
     * @param userId Merchant userId
     * @param principal Currently authenticated user
     * @param merchantUserChangePasswordModel Merchant user change password model
     */
    @Transactional
    fun changePasswordMerchantUser(
        userId: String,
        principal: String,
        merchantUserEditPasswordModel: MerchantUserEditPasswordRequestModel
    ) {
        if (principal != adminUsername && principal != userId) throw ApiError.ofErrorCode(ApiErrorCode.INSUFFICIENT_RIGHTS).asException()

        val merchantUser = merchantUserRepository.findByEmail(userId)
        val isPasswordMatching =
            userPasswordEncoder.matches(merchantUserEditPasswordModel.oldPassword, merchantUser?.password)
        if (isPasswordMatching) merchantUserRepository.updatePasswordMerchantUser(
            userId,
            userPasswordEncoder.encode(merchantUserEditPasswordModel.newPassword)
        ) else throw ApiError.ofErrorCode(ApiErrorCode.INCORRECT_OLD_PASSWORD, "Old password for user '$userId' is incorrect").asException()
    }

    /**
     * Create a merchant user by given data
     *
     * @param merchantId Merchant ID
     * @param merchantUserModel Merchant user model
     */
    @Transactional
    fun createMerchantUser(merchantId: String, merchantUserModel: MerchantUserRequestModel) {
        val authority = authorityRepository.getAuthorityByName(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.NO_RIGHTS, "There is no role defined for merchant '$merchantId'").asException()
        if (merchantUserRepository.findByEmail(merchantUserModel.email) != null) throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_USER_ALREADY_EXISTS, "Merchant user with given email already exists").asException()
        merchantUserRepository.save(merchantUserModel.toMerchantUser(authority))
    }

    /**
     * Send reset password email to user
     *
     * @param email Email
     * @param request Request
     * @param merchantId Merchant ID
     */
    @Transactional
    fun sendForgotPasswordEmail(email: String, request: HttpServletRequest, merchantId: String) {
        val merchantUser = merchantUserRepository.findByEmail(email) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_USER_NOT_FOUND).asException()
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val token = UUID.randomUUID().toString()
        val expiryDate = Date().toInstant().atZone(ZoneId.of(merchant.timezone) ?: ZoneId.systemDefault()).toLocalDateTime().plusDays(1)
        passwordResetTokenRepository.save(PasswordResetToken(token = token, merchantUser = merchantUser, expiryDate = Date.from(expiryDate.atZone(ZoneId.of(merchant.timezone) ?: ZoneId.systemDefault()).toInstant())))
        val baseUrl = request.scheme + "://" + request.serverName + ":" + request.serverPort + request.contextPath
        val body = baseUrl + "/reset-password?token=" + token + "&email=" + email
        sendEmail(email, body)
    }

    /**
     * Validates token and resets user's password
     *
     * @param token Token
     * @param email Email
     * @param merchantUserChangePasswordModel Merchant user change password model
     */
    @Transactional
    fun validateTokenAndResetPassword(token: String, email: String, merchantUserEditPasswordModel: MerchantUserEditPasswordRequestModel) {
        val passwordResetToken = passwordResetTokenRepository.getByToken(token) ?: throw ApiError.ofErrorCode(ApiErrorCode.TOKEN_NOT_FOUND).asException()
        if (passwordResetToken.isExpired()) throw ApiError.ofErrorCode(ApiErrorCode.TOKEN_EXPIRED).asException()
        changePasswordMerchantUser(email, email, merchantUserEditPasswordModel)
    }

    private fun sendEmail(email: String, body: String) {
        val mail = Mail(Email(paymentEmail), "Password reset request", Email(email), Content("text/plain", body))
        val request = Request()
        request.method = Method.POST
        request.endpoint = "mail/send"
        request.body = mail.build()
        sendGrid.api(request)
    }

    private fun MerchantUser.toUserDetails() = User(email, password, enabled, true, true, true, authorities)

    private fun MerchantUserRequestModel.toMerchantUser(authority: Authority) =
        MerchantUser(
            email,
            firstname,
            lastname,
            locale,
            userPasswordEncoder.encode(password),
            true,
            setOf(authority)
        )

    companion object : KLogging()
}
