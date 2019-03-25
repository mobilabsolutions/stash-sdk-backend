package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.PreauthorizeRequestModel
import com.mobilabsolutions.payment.model.PreauthorizeResponseModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
@Transactional
class PreauthorizationService(
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
    fun preauthorize(secretKey: String, idempotentKey: String, preauthorizeInfo: PreauthorizeRequestModel): ResponseEntity<PreauthorizeResponseModel> {
        /**
         * TO-DO: Implement authorization with PSP
         */
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
                ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstById(preauthorizeInfo.aliasId)
                ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val extra = objectMapper.readValue(alias.extra ?: throw ApiError.ofMessage("Used alias is incomplete, please define a payment configuration on related alias").asInternalServerError(), AliasExtraModel::class.java)

        if (extra.paymentMethod != PaymentMethod.CC)
            throw ApiError.ofMessage("Payment method is not of type CC").asBadRequest()

        val paymentInfoModel = PaymentInfoModel(extra,
                objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java))

        when {
            transactionRepository.getIdByIdempotentKey(idempotentKey) == null -> {
                val transaction = Transaction(
                    transactionId = RandomStringUtils.randomAlphanumeric(PreauthorizationService.STRING_LENGTH),
                    idempotentKey = idempotentKey,
                    currencyId = preauthorizeInfo.paymentData.currency,
                    amount = preauthorizeInfo.paymentData.amount,
                    reason = preauthorizeInfo.paymentData.reason,
                    status = TransactionStatus.SUCCESS,
                    action = TransactionAction.PREAUTH,
                    paymentMethod = extra.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    merchantTransactionId = preauthorizeInfo.purchaseId,
                    merchantCustomerId = preauthorizeInfo.customerId,
                    merchant = apiKey.merchant,
                    alias = alias
                )
                transactionRepository.save(transaction)

                return ResponseEntity.status(HttpStatus.CREATED).body(PreauthorizeResponseModel(transaction.transactionId, transaction.amount,
                    transaction.currencyId, transaction.status, transaction.action))
            }
            transactionRepository.getIdByIdempotentKeyAndGivenBody(idempotentKey, preauthorizeInfo) != null -> return ResponseEntity.status(HttpStatus.OK).body(null)
            else -> throw ApiError.ofMessage("There is already a transaction with given idempotent key").asBadRequest()
        }
    }

    companion object : KLogging() {
        const val STRING_LENGTH = 20
    }
}