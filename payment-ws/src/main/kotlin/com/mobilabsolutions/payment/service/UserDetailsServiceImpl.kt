package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.MerchantUser
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.model.request.EditMerchantUserRequestModel
import com.mobilabsolutions.payment.model.request.MerchantUserPasswordRequestModel
import com.mobilabsolutions.payment.model.request.MerchantUserRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
class UserDetailsServiceImpl(
    private val merchantUserRepository: MerchantUserRepository,
    private val authorityRepository: AuthorityRepository,
    @Qualifier("userPasswordEncoder") private val userPasswordEncoder: PasswordEncoder
) : UserDetailsService {

    @Value("\${initial.data.loader.adminUsername}")
    lateinit var adminUsername: String

    /**
     * Find merchant user by id and wrap the merchant user with extra information for authentication
     *
     * @param email Merchant user email
     * @return Enhanced merchant user
     */
    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        return merchantUserRepository.findByEmail(email)?.toUserDetails()
            ?: throw ApiError.builder().withMessage("Bad credentials").build().asUnauthorized()
    }

    /**
     * Update the merchant user by given userId
     *
     * @param userId Merchant userId
     * @param principal Currently authenticated user
     * @param merchantUserModel Merchant user model
     */
    @Transactional
    fun updateMerchantUser(userId: String, principal: String, merchantUserModel: EditMerchantUserRequestModel) {
        if (principal != adminUsername && principal != userId) throw ApiError.ofMessage("Authenticated user doesn't have the required rights for this operation").asForbidden()
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
        merchantUserChangePasswordModel: MerchantUserPasswordRequestModel
    ) {
        if (principal != adminUsername && principal != userId) throw ApiError.ofMessage("Authenticated user doesn't have the required rights for this operation").asForbidden()

        val merchantUser = merchantUserRepository.findByEmail(userId)
        val isPasswordMatching =
            userPasswordEncoder.matches(merchantUserChangePasswordModel.oldPassword, merchantUser?.password)
        if (isPasswordMatching) merchantUserRepository.updatePasswordMerchantUser(
            userId,
            userPasswordEncoder.encode(merchantUserChangePasswordModel.newPassword)
        ) else throw ApiError.ofMessage("Old password for user '$userId' is incorrect").asBadRequest()
    }

    /**
     * Create a merchant user by given data
     *
     * @param merchantUserModel Merchant user model
     */
    @Transactional
    fun createMerchantUser(merchantId: String, merchantUserModel: MerchantUserRequestModel) {
        val authority = authorityRepository.getAuthorityByName(merchantId) ?: throw ApiError.ofMessage("There is no role defined for merchant '$merchantId'").asBadRequest()
        merchantUserRepository.save(merchantUserModel.toMerchantUser(authority))
    }

    private fun MerchantUser.toUserDetails() = User(email, password, enabled, true, true, true, authorities)

    private fun MerchantUserRequestModel.toMerchantUser(authority: Authority) = MerchantUser(
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
