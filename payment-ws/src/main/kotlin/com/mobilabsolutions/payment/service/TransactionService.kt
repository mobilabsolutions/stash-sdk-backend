package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PaymentResponseModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
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
    private val pspRegistry: PspRegistry,
    private val objectMapper: ObjectMapper
) {

    /**
     * Authorize transaction
     *
     * @param secretKey Secret key
     * @param idempotentKey Idempotent key
     * @param authorizeInfo Payment information
     * @return Payment response model
     */
    fun authorize(
        secretKey: String,
        idempotentKey: String,
        authorizeInfo: PaymentRequestModel
    ): PaymentResponseModel {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
                ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(authorizeInfo.aliasId!!, true)
                ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val psp = pspRegistry.find(alias.psp!!)
                ?: throw ApiError.ofMessage("PSP implementation '${alias.psp}' cannot be found").asBadRequest()

        return executeIdempotentTransactionOperation(
                alias,
                apiKey,
                idempotentKey,
                authorizeInfo,
                TransactionAction.AUTH
        ) { psp.authorize(authorizeInfo) }
    }

    /**
     * Preauthorize transaction
     *
     * @param secretKey Secret key
     * @param idempotentKey Idempotent key
     * @param preauthorizeInfo Payment information
     * @return Payment response model
     */
    fun preauthorize(
        secretKey: String,
        idempotentKey: String,
        preauthorizeInfo: PaymentRequestModel
    ): PaymentResponseModel {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(preauthorizeInfo.aliasId!!, true)
            ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val psp = pspRegistry.find(alias.psp!!)
            ?: throw ApiError.ofMessage("PSP implementation '${alias.psp}' cannot be found").asBadRequest()

        return executeIdempotentTransactionOperation(
            alias,
            apiKey,
            idempotentKey,
            preauthorizeInfo,
            TransactionAction.PREAUTH
        ) { psp.preauthorize(preauthorizeInfo) }
    }

    /**
     * Capture transaction
     *
     * @param secretKey Secret key
     * @param transactionId Transaction ID
     * @return Payment response model
     */
    fun capture(secretKey: String, transactionId: String): PaymentResponseModel {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val preauthTransaction = transactionRepository.getByTransactionIdAndAction(
            transactionId,
            TransactionAction.PREAUTH,
            TransactionStatus.SUCCESS
        )
            ?: throw ApiError.ofMessage("Transaction cannot be found").asBadRequest()
        if (preauthTransaction.merchant.id != apiKey.merchant.id)
            throw ApiError.ofMessage("Api key is correct but does not map to correct merchant").asBadRequest()

        val paymentInfoModel = PaymentInfoModel(
            objectMapper.readValue(preauthTransaction.alias?.extra, AliasExtraModel::class.java),
            objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java)
        )

        val captureTransaction =
            transactionRepository.getByTransactionIdAndAction(transactionId, TransactionAction.CAPTURE)

        when {
            captureTransaction != null -> return PaymentResponseModel(
                captureTransaction.transactionId,
                captureTransaction.amount,
                captureTransaction.currencyId,
                captureTransaction.status,
                captureTransaction.action,
                objectMapper.readValue(
                    captureTransaction.pspResponse,
                    PspPaymentResponseModel::class.java
                )?.errorMessage
            )
            else -> {
                val pspPaymentResponse = PspPaymentResponseModel(
                    "test",
                    TransactionStatus.SUCCESS,
                    null,
                    null,
                    null
                ) // TODO psp.capture invocation
                val newTransaction = Transaction(
                    transactionId = transactionId,
                    idempotentKey = preauthTransaction.idempotentKey,
                    currencyId = preauthTransaction.currencyId,
                    amount = preauthTransaction.amount,
                    reason = preauthTransaction.reason,
                    status = TransactionStatus.SUCCESS,
                    action = TransactionAction.CAPTURE,
                    paymentMethod = preauthTransaction.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    pspResponse = objectMapper.writeValueAsString(pspPaymentResponse),
                    merchantTransactionId = preauthTransaction.merchantTransactionId,
                    merchantCustomerId = preauthTransaction.merchantCustomerId,
                    merchant = preauthTransaction.merchant,
                    alias = preauthTransaction.alias
                )
                transactionRepository.save(newTransaction)

                return PaymentResponseModel(
                    newTransaction.transactionId,
                    newTransaction.amount,
                    newTransaction.currencyId,
                    newTransaction.status,
                    newTransaction.action,
                    pspPaymentResponse.errorMessage
                )
            }
        }
    }

    private fun executeIdempotentTransactionOperation(
        alias: Alias,
        apiKey: MerchantApiKey,
        idempotentKey: String,
        paymentInfo: PaymentRequestModel,
        transactionAction: TransactionAction,
        pspAction: ((requestModel: PaymentRequestModel) -> PspPaymentResponseModel)
    ): PaymentResponseModel {
        val extra = objectMapper.readValue(
            alias.extra
                ?: throw ApiError.ofMessage("Used alias is incomplete, please define a payment configuration on related alias").asBadRequest(),
            AliasExtraModel::class.java
        )

        val paymentInfoModel =
            PaymentInfoModel(extra, objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java))

        val transaction = transactionRepository.getByIdempotentKeyAndAction(idempotentKey, transactionAction)

        when {
            transaction != null -> return PaymentResponseModel(
                transaction.transactionId,
                transaction.amount,
                transaction.currencyId,
                transaction.status,
                transaction.action,
                objectMapper.readValue(transaction.pspResponse, PspPaymentResponseModel::class.java)?.errorMessage
            )

            else -> {
                val pspPaymentResponse = pspAction.invoke(paymentInfo)
                val newTransaction = Transaction(
                    transactionId = RandomStringUtils.randomAlphanumeric(TransactionService.TRANSACTION_ID_LENGTH),
                    idempotentKey = idempotentKey,
                    currencyId = paymentInfo.paymentData!!.currency,
                    amount = paymentInfo.paymentData!!.amount,
                    reason = paymentInfo.paymentData!!.reason,
                    status = pspPaymentResponse.status ?: TransactionStatus.FAIL,
                    action = transactionAction,
                    paymentMethod = extra.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    pspResponse = objectMapper.writeValueAsString(pspPaymentResponse),
                    merchantTransactionId = paymentInfo.purchaseId,
                    merchantCustomerId = paymentInfo.customerId,
                    merchant = apiKey.merchant,
                    alias = alias
                )
                transactionRepository.save(newTransaction)

                return PaymentResponseModel(
                    newTransaction.transactionId, newTransaction.amount,
                    newTransaction.currencyId, newTransaction.status,
                    newTransaction.action, pspPaymentResponse.errorMessage
                )
            }
        }
    }

    companion object : KLogging() {
        const val TRANSACTION_ID_LENGTH = 20
    }
}