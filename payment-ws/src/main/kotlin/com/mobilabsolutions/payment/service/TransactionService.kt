package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
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
     * @param pspTestMode indicator whether is the test mode or not
     * @param authorizeInfo Payment information
     * @return Payment response model
     */
    fun authorize(
        secretKey: String,
        idempotentKey: String,
        pspTestMode: Boolean?,
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
            pspTestMode,
            TransactionAction.AUTH
        ) { psp.authorize(authorizeInfo, pspTestMode) }
    }

    /**
     * Preauthorize transaction
     *
     * @param secretKey Secret key
     * @param idempotentKey Idempotent key
     * @param pspTestMode indicator whether is the test mode or not
     * @param preauthorizeInfo Payment information
     * @return Payment response model
     */
    fun preauthorize(
        secretKey: String,
        idempotentKey: String,
        pspTestMode: Boolean?,
        preauthorizeInfo: PaymentRequestModel
    ): PaymentResponseModel {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(preauthorizeInfo.aliasId!!, true)
            ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val psp = pspRegistry.find(alias.psp!!)
            ?: throw ApiError.ofMessage("PSP implementation '${alias.psp}' cannot be found").asBadRequest()
        if (getAliasExtra(alias).paymentMethod != PaymentMethod.CC)
            throw ApiError.ofMessage("Only credit card is supported for preauthorization").asBadRequest()

        return executeIdempotentTransactionOperation(
            alias,
            apiKey,
            idempotentKey,
            preauthorizeInfo,
            pspTestMode,
            TransactionAction.PREAUTH
        ) { psp.preauthorize(preauthorizeInfo, pspTestMode) }
    }

    /**
     * Capture transaction
     *
     * @param secretKey Secret key
     * @param pspTestMode indicator whether is the test mode or not
     * @param transactionId Transaction ID
     * @return Payment response model
     */
    fun capture(secretKey: String, pspTestMode: Boolean?, transactionId: String): PaymentResponseModel {
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

        val testMode = pspTestMode ?: false
        if (testMode != preauthTransaction.pspTestMode)
            throw ApiError.ofMessage("PSP test mode for this transaction is different than the mode for preauthorization transaction. Please, check your header").asBadRequest()

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
                val psp = pspRegistry.find(preauthTransaction.alias?.psp!!)
                    ?: throw ApiError.ofMessage("PSP implementation '${preauthTransaction.alias?.psp}' cannot be found").asBadRequest()
                val pspResponseTransactionInfo = objectMapper.readValue(preauthTransaction.pspResponse, PspPaymentResponseModel::class.java)
                val pspPaymentResponse = psp.capture(preauthTransaction.transactionId!!, pspResponseTransactionInfo.pspTransactionId, pspTestMode)
                val newTransaction = Transaction(
                    transactionId = transactionId,
                    idempotentKey = preauthTransaction.idempotentKey,
                    currencyId = preauthTransaction.currencyId,
                    amount = preauthTransaction.amount,
                    reason = preauthTransaction.reason,
                    status = pspPaymentResponse.status,
                    action = TransactionAction.CAPTURE,
                    paymentMethod = preauthTransaction.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    pspResponse = objectMapper.writeValueAsString(pspPaymentResponse),
                    merchantTransactionId = preauthTransaction.merchantTransactionId,
                    merchantCustomerId = preauthTransaction.merchantCustomerId,
                    pspTestMode = pspTestMode ?: preauthTransaction.pspTestMode,
                    merchant = preauthTransaction.merchant,
                    alias = preauthTransaction.alias
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

    private fun executeIdempotentTransactionOperation(
        alias: Alias,
        apiKey: MerchantApiKey,
        idempotentKey: String,
        paymentInfo: PaymentRequestModel,
        pspTestMode: Boolean?,
        transactionAction: TransactionAction,
        pspAction: ((requestModel: PaymentRequestModel) -> PspPaymentResponseModel)
    ): PaymentResponseModel {
        val extra = getAliasExtra(alias)

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
                    pspTestMode = pspTestMode ?: false,
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

    private fun getAliasExtra(alias: Alias): AliasExtraModel {
        return objectMapper.readValue(alias.extra
                ?: throw ApiError.ofMessage("Used alias is incomplete, please define a payment configuration on related alias").asBadRequest(),
            AliasExtraModel::class.java
        )
    }

    companion object : KLogging() {
        const val TRANSACTION_ID_LENGTH = 20
    }
}
