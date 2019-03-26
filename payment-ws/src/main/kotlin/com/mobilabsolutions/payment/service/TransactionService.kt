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
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
@Transactional
class TransactionService(
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
    fun authorize(secretKey: String, idempotentKey: String, authorizeInfo: AuthorizeRequestModel): ResponseEntity<AuthorizeResponseModel> {
        return executeIdempotentTransactionOperation(secretKey, idempotentKey, authorizeInfo, TransactionAction.AUTH)
    }

    /**
     * Preauthorize transaction
     *
     * @param secretKey Secret key
     * @param idempotentKey Idempotent key
     * @param authorizeInfo Authorization information
     * @return Authorization response model
     */
    fun preauthorize(secretKey: String, idempotentKey: String, authorizeInfo: AuthorizeRequestModel): ResponseEntity<AuthorizeResponseModel> {
        return executeIdempotentTransactionOperation(secretKey, idempotentKey, authorizeInfo, TransactionAction.PREAUTH)
    }

    private fun executeIdempotentTransactionOperation(secretKey: String, idempotentKey: String, authorizeInfo: AuthorizeRequestModel, transactionAction: TransactionAction): ResponseEntity<AuthorizeResponseModel> {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstById(authorizeInfo.aliasId)
            ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val extra = objectMapper.readValue(alias.extra
            ?: throw ApiError.ofMessage("Used alias is incomplete, please define a payment configuration on related alias").asInternalServerError(), AliasExtraModel::class.java)

        val paymentInfoModel = PaymentInfoModel(extra,
            objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java))

        when {
            transactionRepository.getIdByIdempotentKeyAndAction(idempotentKey, TransactionAction.AUTH) == null -> {
                val transaction = Transaction(
                    transactionId = RandomStringUtils.randomAlphanumeric(PreauthorizationService.STRING_LENGTH),
                    idempotentKey = idempotentKey,
                    currencyId = authorizeInfo.paymentData.currency,
                    amount = authorizeInfo.paymentData.amount,
                    reason = authorizeInfo.paymentData.reason,
                    status = TransactionStatus.SUCCESS,
                    action = TransactionAction.AUTH,
                    paymentMethod = extra.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    merchantTransactionId = authorizeInfo.purchaseId,
                    merchantCustomerId = authorizeInfo.customerId,
                    merchant = apiKey.merchant,
                    alias = alias
                )
                transactionRepository.save(transaction)

                return ResponseEntity.status(HttpStatus.CREATED).body(
                    AuthorizeResponseModel(transaction.transactionId, transaction.amount,
                        transaction.currencyId, transaction.status, transaction.action)
                )
            }
            transactionRepository.getIdByIdempotentKeyAndActionAndGivenBody(idempotentKey, TransactionAction.AUTH, authorizeInfo) != null -> return ResponseEntity.status(HttpStatus.OK).body(null)
            else -> throw ApiError.ofMessage("There is already a transaction with given idempotent key").asBadRequest()
        }
    }
}