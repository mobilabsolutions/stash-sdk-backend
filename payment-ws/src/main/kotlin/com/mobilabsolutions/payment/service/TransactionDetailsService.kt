package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.TransactionModel
import com.mobilabsolutions.payment.model.response.TransactionDetailsResponseModel
import com.mobilabsolutions.payment.model.response.TransactionListResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    /**
     * Get transaction by ID
     *
     * @param merchantId Merchant ID
     * @param transactionId Transaction ID
     * @return transaction details by id response
     */
    fun getTransaction(merchantId: String, transactionId: String): TransactionDetailsResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transaction = transactionRepository.getByTransactionId(transactionId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_FOUND).asException()

        return TransactionDetailsResponseModel(
            transaction.transactionId,
            transaction.currencyId,
            transaction.amount.toString(),
            transaction.reason,
            transaction.action,
            transaction.status,
            transaction.paymentMethod,
            objectMapper.readValue(transaction.paymentInfo, PaymentInfoModel::class.java),
            transaction.merchantTransactionId,
            transaction.merchantCustomerId,
            transaction.pspTestMode,
            transaction.merchant.id,
            transaction.alias!!.id
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
            action, status, text, limit ?: 10, offset ?: 0)

        val transactionList = TransactionListResponseModel(transactions.asSequence().map { TransactionModel(it) }.toMutableList())
        if (transactionList.transactions.isEmpty()) throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTIONS_NOT_FOUND).asException()
        return transactionList
    }
}
