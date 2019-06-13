package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.TransactionTimelineModel
import com.mobilabsolutions.payment.model.response.TransactionDetailsResponseModel
import com.mobilabsolutions.payment.model.response.TransactionListResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
@Transactional
class TransactionDetailsService(
    private val transactionRepository: TransactionRepository,
    private val merchantRepository: MerchantRepository,
    private val objectMapper: ObjectMapper
) {

    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("Europe/Berlin"))

    /**
     * Get transaction by ID
     *
     * @param merchantId Merchant ID
     * @param transactionId Transaction ID
     * @return transaction details by id response
     */
    fun getTransactionDetails(merchantId: String, transactionId: String): TransactionDetailsResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transaction = transactionRepository.getByTransactionId(transactionId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_FOUND).asException()
        val timelineTransactions = transactionRepository.getTransactionDetails(transactionId)

        return TransactionDetailsResponseModel(
            dateTimeFormatter.format(transaction.createdDate),
            transaction.transactionId,
            transaction.currencyId,
            transaction.amount,
            transaction.reason,
            transaction.action!!.name,
            transaction.status!!.name,
            transaction.paymentMethod!!.name,
            objectMapper.readValue(transaction.paymentInfo, PaymentInfoModel::class.java),
            transaction.merchantTransactionId,
            transaction.merchantCustomerId,
            transaction.pspTestMode,
            transaction.merchant.id,
            transaction.alias!!.id,
            timelineTransactions.asSequence().map { TransactionTimelineModel(it) }.toMutableList()
        )
    }

    /**
     * Get transactions by given filters
     *
     * @param merchantId
     * @param createdAtStart created date start
     * @param createdAtEnd created date end
     * @param paymentMethod payment method
     * @param action action
     * @param status status
     * @param text any transaction related information
     * @param limit requested transaction list limit
     * @param offset requested transaction list offset
     * @return filtered transaction list
     */
    fun getTransactionsByFilters(
        merchantId: String,
        createdAtStart: String?,
        createdAtEnd: String?,
        paymentMethod: String?,
        action: String?,
        status: String?,
        text: String?,
        limit: Int?,
        offset: Int?
    ): TransactionListResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsByFilters(merchantId, createdAtStart, createdAtEnd, paymentMethod,
            action, status, text, limit, offset)

        val transactionList = TransactionListResponseModel(transactions, offset, limit)
        if (transactionList.transactions.isEmpty()) throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTIONS_NOT_FOUND).asException()
        return transactionList
    }
}
