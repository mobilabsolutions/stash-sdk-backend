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
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
import com.mobilabsolutions.payment.model.request.ReversalRequestModel
import com.mobilabsolutions.payment.model.response.PaymentResponseModel
import com.mobilabsolutions.payment.model.response.PspPaymentResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
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
                ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()
        val alias = aliasRepository.getFirstByIdAndActive(authorizeInfo.aliasId!!, true)
                ?: throw ApiError.ofErrorCode(ApiErrorCode.ALIAS_NOT_FOUND).asException()
        val psp = pspRegistry.find(alias.psp!!)
                ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${alias.psp}' cannot be found").asException()

        val pspAuthorizeRequest = PspPaymentRequestModel(
            aliasId = authorizeInfo.aliasId,
            paymentData = authorizeInfo.paymentData,
            extra = getAliasExtra(alias),
            pspAlias = alias.pspAlias,
            pspConfig = getPspConfig(alias),
            purchaseId = authorizeInfo.purchaseId
        )
        return executeIdempotentTransactionOperation(
            alias = alias,
            apiKey = apiKey,
            idempotentKey = idempotentKey,
            paymentInfo = authorizeInfo,
            pspTestMode = pspTestMode,
            transactionAction = TransactionAction.AUTH
        ) { psp.authorize(pspAuthorizeRequest, pspTestMode) }
    }

    /**
     * Preauthorize transaction
     *
     * @param secretKey Secret key
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
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()
        val alias = aliasRepository.getFirstByIdAndActive(preauthorizeInfo.aliasId!!, true)
            ?: throw throw ApiError.ofErrorCode(ApiErrorCode.ALIAS_NOT_FOUND).asException()
        val psp = pspRegistry.find(alias.psp!!)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${alias.psp}' cannot be found").asException()
        if (getAliasExtra(alias).paymentMethod == PaymentMethod.SEPA.name)
            throw ApiError.ofErrorCode(ApiErrorCode.SEPA_NOT_ALLOWED).asException()

        val pspPreauthorizeRequest = PspPaymentRequestModel(
            aliasId = preauthorizeInfo.aliasId,
            paymentData = preauthorizeInfo.paymentData,
            extra = getAliasExtra(alias),
            pspAlias = alias.pspAlias,
            pspConfig = getPspConfig(alias),
            purchaseId = preauthorizeInfo.purchaseId
        )
        return executeIdempotentTransactionOperation(
            alias = alias,
            apiKey = apiKey,
            idempotentKey = idempotentKey,
            paymentInfo = preauthorizeInfo,
            pspTestMode = pspTestMode,
            transactionAction = TransactionAction.PREAUTH
        ) { psp.preauthorize(pspPreauthorizeRequest, pspTestMode) }
    }

    /**
     * Capture transaction
     *
     * @param secretKey Secret key
     * @param pspTestMode indicator whether is the test mode or not
     * @param transactionId Transaction ID
     * @return Payment response model
     */
    fun capture(
        secretKey: String,
        pspTestMode: Boolean?,
        transactionId: String
    ): PaymentResponseModel {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()
        val lastTransaction = transactionRepository.getByTransactionIdAndStatus(
            transactionId,
            TransactionStatus.SUCCESS.toString()
        )
            ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_FOUND).asException()
        if (lastTransaction.action != TransactionAction.PREAUTH && lastTransaction.action != TransactionAction.CAPTURE)
            throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_ALLOWED, "${lastTransaction.action} transaction cannot be captured").asException()

        if (lastTransaction.merchant.id != apiKey.merchant.id)
            throw ApiError.ofErrorCode(ApiErrorCode.WRONG_ALIAS_MERCHANT_MAPPING).asException()

        val testMode = pspTestMode ?: false
        if (testMode != lastTransaction.pspTestMode)
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_TEST_MODE_INCONSISTENT).asException()

        when {
            lastTransaction.action == TransactionAction.CAPTURE -> return PaymentResponseModel(
                lastTransaction.transactionId,
                lastTransaction.amount,
                lastTransaction.currencyId,
                lastTransaction.status,
                lastTransaction.action,
                objectMapper.readValue(
                    lastTransaction.pspResponse,
                    PspPaymentResponseModel::class.java
                )?.errorMessage
            )
            else -> {
                val psp = pspRegistry.find(lastTransaction.alias?.psp!!)
                    ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${lastTransaction.alias?.psp}' cannot be found").asException()
                val pspCaptureRequest = PspCaptureRequestModel(
                    pspTransactionId = getPspPaymentResponse(lastTransaction).pspTransactionId,
                    amount = lastTransaction.amount,
                    currency = lastTransaction.currencyId,
                    pspConfig = getPspConfig(lastTransaction.alias!!),
                    purchaseId = lastTransaction.merchantTransactionId
                )
                val pspPaymentResponse = psp.capture(pspCaptureRequest, pspTestMode)
                val newTransaction = Transaction(
                    transactionId = transactionId,
                    idempotentKey = lastTransaction.idempotentKey,
                    currencyId = lastTransaction.currencyId,
                    amount = lastTransaction.amount,
                    reason = lastTransaction.reason,
                    status = pspPaymentResponse.status,
                    action = TransactionAction.CAPTURE,
                    paymentMethod = lastTransaction.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(readPaymentInfo(lastTransaction)),
                    pspResponse = objectMapper.writeValueAsString(pspPaymentResponse),
                    merchantTransactionId = lastTransaction.merchantTransactionId,
                    merchantCustomerId = lastTransaction.merchantCustomerId,
                    pspTestMode = pspTestMode ?: lastTransaction.pspTestMode,
                    merchant = lastTransaction.merchant,
                    alias = lastTransaction.alias
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

    /**
     * Reverse transaction
     *
     * @param secretKey Secret Key
     * @param pspTestMode indicator whether is the test mode or not
     * @param transactionId Transaction ID
     * @param reverseInfo Reversion request model
     * @return Payment response model
     */
    fun reverse(
        secretKey: String,
        pspTestMode: Boolean?,
        transactionId: String,
        reverseInfo: ReversalRequestModel
    ): PaymentResponseModel {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()
        val lastTransaction = transactionRepository.getByTransactionIdAndStatus(
            transactionId,
            TransactionStatus.SUCCESS.toString()
        )
            ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_FOUND).asException()
        if (lastTransaction.action != TransactionAction.PREAUTH && lastTransaction.action != TransactionAction.REVERSAL)
            throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_ALLOWED, "${lastTransaction.action} transaction cannot be reversed").asException()

        if (lastTransaction.merchant.id != apiKey.merchant.id)
            throw ApiError.ofErrorCode(ApiErrorCode.WRONG_ALIAS_MERCHANT_MAPPING).asException()

        val testMode = pspTestMode ?: false
        if (testMode != lastTransaction.pspTestMode)
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_TEST_MODE_INCONSISTENT).asException()

        when {
            lastTransaction.action == TransactionAction.REVERSAL -> return PaymentResponseModel(
                lastTransaction.transactionId,
                lastTransaction.amount,
                lastTransaction.currencyId,
                lastTransaction.status,
                lastTransaction.action,
                objectMapper.readValue(
                    lastTransaction.pspResponse,
                    PspPaymentResponseModel::class.java
                )?.errorMessage
            )
            else -> {
                val psp = pspRegistry.find(lastTransaction.alias?.psp!!)
                    ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${lastTransaction.alias?.psp}' cannot be found").asException()
                val pspReversalRequest = PspReversalRequestModel(
                    pspTransactionId = getPspPaymentResponse(lastTransaction).pspTransactionId,
                    currency = lastTransaction.currencyId,
                    pspConfig = getPspConfig(lastTransaction.alias!!),
                    purchaseId = lastTransaction.merchantTransactionId
                )
                val pspReversalResponse = psp.reverse(pspReversalRequest, pspTestMode)
                val newTransaction = Transaction(
                    transactionId = transactionId,
                    idempotentKey = lastTransaction.idempotentKey,
                    currencyId = lastTransaction.currencyId,
                    amount = lastTransaction.amount,
                    reason = reverseInfo.reason,
                    status = pspReversalResponse.status,
                    action = TransactionAction.REVERSAL,
                    paymentMethod = lastTransaction.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(readPaymentInfo(lastTransaction)),
                    pspResponse = objectMapper.writeValueAsString(pspReversalResponse),
                    merchantTransactionId = lastTransaction.merchantTransactionId,
                    merchantCustomerId = lastTransaction.merchantCustomerId,
                    pspTestMode = pspTestMode ?: lastTransaction.pspTestMode,
                    merchant = lastTransaction.merchant,
                    alias = lastTransaction.alias
                )
                transactionRepository.save(newTransaction)

                return PaymentResponseModel(
                    newTransaction.transactionId, newTransaction.amount,
                    newTransaction.currencyId, newTransaction.status,
                    newTransaction.action, pspReversalResponse.errorMessage
                )
            }
        }
    }

    /**
     * Refund transaction
     *
     * @param secretKey Secret Key
     * @param idempotentKey Idempotent key
     * @param pspTestMode indicator whether is the test mode or not
     * @param transactionId Transaction ID
     * @param refundInfo Payment information
     * @return Payment response model
     */
    fun refund(
        secretKey: String,
        idempotentKey: String,
        pspTestMode: Boolean?,
        transactionId: String,
        refundInfo: PaymentDataRequestModel
    ): PaymentResponseModel {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()

        val transactions = transactionRepository.getListByTransactionIdAndStatus(transactionId, TransactionStatus.SUCCESS.name)

        val prevTransaction = transactions.asSequence().filter { TransactionAction.CAPTURE == it.action || TransactionAction.AUTH == it.action }.firstOrNull() ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_ALLOWED).asException()

        val originalTransaction = transactions.asSequence().filter { TransactionAction.PREAUTH == it.action || TransactionAction.AUTH == it.action }.firstOrNull()

        val alias = prevTransaction.alias
        val psp = pspRegistry.find(alias!!.psp!!)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${alias.psp}' cannot be found").asException()

        if (prevTransaction.merchant.id != apiKey.merchant.id)
            throw ApiError.ofErrorCode(ApiErrorCode.WRONG_ALIAS_MERCHANT_MAPPING).asException()

        val allRefundTransactions = transactions.asSequence().filter { TransactionAction.REFUND == it.action }.toMutableList()

        if (!checkRefundEligibility(prevTransaction.amount!!, refundInfo.amount!!, allRefundTransactions))
            throw ApiError.ofErrorCode(ApiErrorCode.INCORRECT_REFUND_VALUE).asException()

        val paymentRequestModel = PaymentRequestModel(
            alias.id,
            refundInfo,
            prevTransaction.merchantTransactionId,
            prevTransaction.merchantCustomerId
        )
        val pspRefundRequest = PspRefundRequestModel(
            pspTransactionId = getPspPaymentResponse(originalTransaction).pspTransactionId,
            amount = refundInfo.amount,
            currency = prevTransaction.currencyId,
            action = prevTransaction.action,
            pspConfig = getPspConfig(prevTransaction.alias!!),
            purchaseId = prevTransaction.merchantTransactionId,
            paymentMethod = prevTransaction.paymentMethod!!.name
        )

        return executeIdempotentTransactionOperation(
            alias,
            apiKey,
            idempotentKey,
            prevTransaction.transactionId,
            paymentRequestModel,
            pspTestMode,
            TransactionAction.REFUND
        ) { psp.refund(pspRefundRequest, pspTestMode) }
    }

    private fun checkRefundEligibility(prevTotalAmount: Int, refundAmount: Int, allTransactions: List<Transaction>): Boolean {
        var prevRefundAmount = refundAmount
        for (transaction in allTransactions) prevRefundAmount += transaction.amount!!
        if (prevRefundAmount > prevTotalAmount) return false
        return true
    }

    private fun executeIdempotentTransactionOperation(
        alias: Alias,
        apiKey: MerchantApiKey,
        idempotentKey: String,
        transactionId: String? = null,
        paymentInfo: PaymentRequestModel,
        pspTestMode: Boolean?,
        transactionAction: TransactionAction,
        pspAction: ((requestModel: PaymentRequestModel) -> PspPaymentResponseModel)
    ): PaymentResponseModel {
        val extra = getAliasExtra(alias)

        val paymentInfoModel =
            PaymentInfoModel(
                extra,
                getPspConfig(alias)
            )

        val transaction = transactionRepository.getByIdempotentKeyAndActionAndMerchantAndAlias(idempotentKey, transactionAction, apiKey.merchant, alias)

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
                    transactionId = transactionId ?: RandomStringUtils.randomAlphanumeric(TransactionService.TRANSACTION_ID_LENGTH),
                    idempotentKey = idempotentKey,
                    currencyId = paymentInfo.paymentData!!.currency,
                    amount = paymentInfo.paymentData.amount,
                    reason = paymentInfo.paymentData.reason,
                    status = pspPaymentResponse.status ?: TransactionStatus.FAIL,
                    action = transactionAction,
                    paymentMethod = PaymentMethod.valueOf(extra.paymentMethod!!),
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
                ?: throw ApiError.ofErrorCode(ApiErrorCode.INCOMPLETE_ALIAS).asException(),
            AliasExtraModel::class.java
        )
    }

    private fun readPaymentInfo(transaction: Transaction): PaymentInfoModel {
        return PaymentInfoModel(
            objectMapper.readValue(transaction.alias?.extra, AliasExtraModel::class.java),
            getPspConfig(transaction.alias!!)
        )
    }

    private fun getPspPaymentResponse(transaction: Transaction?): PspPaymentResponseModel {
        return objectMapper.readValue(transaction?.pspResponse, PspPaymentResponseModel::class.java)
    }

    private fun getPspConfig(alias: Alias): PspConfigModel {
        val result = objectMapper.readValue(alias.merchant?.pspConfig, PspConfigListModel::class.java)
        return result.psp.firstOrNull { it.type == alias.psp!!.toString() }
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_NOT_FOUND, "PSP configuration for '${alias.psp}' cannot be found from used merchant").asException()
    }

    companion object : KLogging() {
        const val TRANSACTION_ID_LENGTH = 20
    }
}
