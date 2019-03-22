package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.AuthorizeRequestModel
import com.mobilabsolutions.payment.model.AuthorizeResponseModel
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
@Transactional
class AuthorizationService(
    private val transactionRepository: TransactionRepository,
    private val merchantApiKeyRepository: MerchantApiKeyRepository,
    private val aliasRepository: AliasRepository,
    private val objectMapper: ObjectMapper
) {
    /**
     * Authorize transaction
     *
     * @param secretKey Secret key
     * @param idempotentKey Idempotent key
     * @param authorizeInfo Authorization information
     * @return Authorization response model
     */
    fun authorize(secretKey: String, idempotentKey: String, authorizeInfo: AuthorizeRequestModel): AuthorizeResponseModel {
        /**
         * TO-DO: Implement authorization with PSP
         */
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
                ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstById(authorizeInfo.aliasId)
                ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()

        val paymentInfoModel = PaymentInfoModel(objectMapper.readValue(alias.extra, AliasExtraModel::class.java),
                objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java))

        val transactionId = RandomStringUtils.randomAlphanumeric(AuthorizationService.STRING_LENGTH)
        val transaction = transactionRepository.getTransactionByIdempotentKey(idempotentKey)
                ?: Transaction(
                        transactionId = transactionId,
                        idempotentKey = idempotentKey,
                        currencyId = authorizeInfo.paymentData.currency,
                        amount = authorizeInfo.paymentData.amount,
                        reason = authorizeInfo.paymentData.reason,
                        status = TransactionStatus.SUCCESS,
                        paymentMethod = objectMapper.readValue(alias.extra, AliasExtraModel::class.java).paymentMethod,
                        paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                        merchantTransactionId = authorizeInfo.purchaseId,
                        merchantCustomerId = authorizeInfo.customerId,
                        merchant = apiKey.merchant,
                        alias = alias
                )

        transaction.action = TransactionAction.AUTH
        transactionRepository.save(transaction)

        return AuthorizeResponseModel(transactionId, transaction.amount,
                transaction.currencyId, transaction.status, transaction.action)
    }

    companion object : KLogging() {
        const val STRING_LENGTH = 20
    }
}