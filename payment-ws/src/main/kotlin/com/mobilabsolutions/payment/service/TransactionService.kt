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
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PaymentResponseModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiException
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
@Transactional(noRollbackFor = [ApiException::class])
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
    fun authorize(secretKey: String, idempotentKey: String, pspTestMode: Boolean?, authorizeInfo: PaymentRequestModel): ResponseEntity<PaymentResponseModel> {
        return executeIdempotentTransactionOperation(secretKey, idempotentKey, authorizeInfo, TransactionAction.AUTH, pspTestMode)
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
    fun preauthorize(secretKey: String, idempotentKey: String, pspTestMode: Boolean?, preauthorizeInfo: PaymentRequestModel): ResponseEntity<PaymentResponseModel> {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(preauthorizeInfo.aliasId!!, true)
            ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val extra = objectMapper.readValue(alias.extra
            ?: throw ApiError.ofMessage("Used alias is incomplete, please define a payment configuration on related alias").asBadRequest(), AliasExtraModel::class.java)

        val paymentInfoModel = PaymentInfoModel(extra, objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java))

        val psp = pspRegistry.find(alias.psp!!)
            ?: throw ApiError.ofMessage("PSP implementation '${alias.psp}' cannot be found").asBadRequest()
        val pspPaymentResponse = psp.preauthorize(preauthorizeInfo, pspTestMode)

        when {
            transactionRepository.getIdByIdempotentKeyAndAction(idempotentKey, TransactionAction.PREAUTH) == null -> {
                val transaction = Transaction(
                    transactionId = RandomStringUtils.randomAlphanumeric(TransactionService.TRANSACTION_ID_LENGTH),
                    idempotentKey = idempotentKey,
                    currencyId = preauthorizeInfo.paymentData!!.currency,
                    amount = preauthorizeInfo.paymentData!!.amount,
                    reason = preauthorizeInfo.paymentData!!.reason,
                    status = pspPaymentResponse.status,
                    action = TransactionAction.PREAUTH,
                    paymentMethod = extra.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    pspTransactionId = pspPaymentResponse.pspTransactionId,
                    merchantTransactionId = preauthorizeInfo.purchaseId,
                    merchantCustomerId = preauthorizeInfo.customerId,
                    pspTestMode = pspTestMode,
                    merchant = apiKey.merchant,
                    alias = alias
                )
                transactionRepository.save(transaction)

                if (pspPaymentResponse.hasError())
                    throw ApiError.builder()
                        .withMessage(pspPaymentResponse.error?.error!!)
                        .withProperty("pspError", pspPaymentResponse.errorMessage!!)
                        .build().asBadRequest()

                return ResponseEntity.status(HttpStatus.CREATED).body(
                    PaymentResponseModel(transaction.transactionId, transaction.amount,
                        transaction.currencyId, transaction.status, transaction.action)
                )
            }
            transactionRepository.getIdByIdempotentKeyAndActionAndGivenBody(idempotentKey, TransactionAction.PREAUTH, preauthorizeInfo) != null -> return ResponseEntity.status(HttpStatus.OK).body(null)
            else -> throw ApiError.ofMessage("There is already a transaction with given idempotent key").asBadRequest()
        }
    }

    /**
     * Capture transaction
     *
     * @param secretKey Secret key
     * @param pspTestMode indicator whether is the test mode or not
     * @param transactionId Transaction ID
     * @return Payment response model
     */
    fun capture(secretKey: String, pspTestMode: Boolean?, transactionId: String): ResponseEntity<PaymentResponseModel> {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val preauthTransaction = transactionRepository.getByTransactionIdAndAction(transactionId, TransactionAction.PREAUTH)
            ?: throw ApiError.ofMessage("Transaction cannot be found").asBadRequest()
        if (preauthTransaction.merchant.id != apiKey.merchant.id)
            throw ApiError.ofMessage("Api key is correct but does not map to correct merchant").asBadRequest()

        val paymentInfoModel = PaymentInfoModel(objectMapper.readValue(preauthTransaction.alias?.extra, AliasExtraModel::class.java), objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java))

        when {
            transactionRepository.getByTransactionIdAndAction(transactionId, TransactionAction.CAPTURE) == null -> {
                val captureTransaction = Transaction(
                    transactionId = transactionId,
                    idempotentKey = preauthTransaction.idempotentKey,
                    currencyId = preauthTransaction.currencyId,
                    amount = preauthTransaction.amount,
                    reason = preauthTransaction.reason,
                    status = TransactionStatus.SUCCESS,
                    action = TransactionAction.CAPTURE,
                    paymentMethod = preauthTransaction.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    merchantTransactionId = preauthTransaction.merchantTransactionId,
                    merchantCustomerId = preauthTransaction.merchantCustomerId,
                    pspTestMode = pspTestMode,
                    merchant = preauthTransaction.merchant,
                    alias = preauthTransaction.alias
                )
                transactionRepository.save(captureTransaction)

                return ResponseEntity.status(HttpStatus.OK).body(PaymentResponseModel(captureTransaction.transactionId, captureTransaction.amount, captureTransaction.currencyId, captureTransaction.status, captureTransaction.action))
            }
            else -> return ResponseEntity.status(HttpStatus.OK).body(null)
        }
    }

    private fun executeIdempotentTransactionOperation(secretKey: String, idempotentKey: String, paymentInfo: PaymentRequestModel, transactionAction: TransactionAction, pspTestMode: Boolean?): ResponseEntity<PaymentResponseModel> {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey)
            ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(paymentInfo.aliasId!!, true)
            ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val extra = objectMapper.readValue(alias.extra
            ?: throw ApiError.ofMessage("Used alias is incomplete, please define a payment configuration on related alias").asBadRequest(), AliasExtraModel::class.java)

        val paymentInfoModel = PaymentInfoModel(extra, objectMapper.readValue(apiKey.merchant.pspConfig, PspConfigListModel::class.java))

        when {
            transactionRepository.getIdByIdempotentKeyAndAction(idempotentKey, transactionAction) == null -> {
                val transaction = Transaction(
                    transactionId = RandomStringUtils.randomAlphanumeric(TransactionService.TRANSACTION_ID_LENGTH),
                    idempotentKey = idempotentKey,
                    currencyId = paymentInfo.paymentData!!.currency,
                    amount = paymentInfo.paymentData!!.amount,
                    reason = paymentInfo.paymentData!!.reason,
                    status = TransactionStatus.SUCCESS,
                    action = transactionAction,
                    paymentMethod = extra.paymentMethod,
                    paymentInfo = objectMapper.writeValueAsString(paymentInfoModel),
                    merchantTransactionId = paymentInfo.purchaseId,
                    merchantCustomerId = paymentInfo.customerId,
                    pspTestMode = pspTestMode,
                    merchant = apiKey.merchant,
                    alias = alias
                )
                transactionRepository.save(transaction)

                return ResponseEntity.status(HttpStatus.CREATED).body(
                    PaymentResponseModel(transaction.transactionId, transaction.amount,
                        transaction.currencyId, transaction.status, transaction.action)
                )
            }
            transactionRepository.getIdByIdempotentKeyAndActionAndGivenBody(idempotentKey, transactionAction, paymentInfo) != null -> return ResponseEntity.status(HttpStatus.OK).body(null)
            else -> throw ApiError.ofMessage("There is already a transaction with given idempotent key").asBadRequest()
        }
    }

    companion object : KLogging() {
        const val TRANSACTION_ID_LENGTH = 20
    }
}