package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.TransactionModel
import com.mobilabsolutions.payment.model.response.TransactionDetailsResponseModel
import com.mobilabsolutions.payment.model.response.TransactionListResponseModel
import com.mobilabsolutions.payment.model.TransactionTimelineModel
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
    fun getTransactionDetails(merchantId: String, transactionId: String): TransactionDetailsResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transaction = transactionRepository.getByTransactionId(transactionId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_FOUND).asException()
        val timelineTransactions = transactionRepository.getTransactionDetails(transactionId)

        return TransactionDetailsResponseModel(
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
     * Get transactions by limit and offset
     *
     * @param merchantId Merchant ID
     * @param limit limit
     * @param offset offset
     * @return transaction list
     */
    fun getTransactions(merchantId: String, limit: Int?, offset: Int?): TransactionListResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsByLimitAndOffset(merchantId, limit ?: 10, offset ?: 0)

        return TransactionListResponseModel(transactions.asSequence().map { TransactionModel(it) }.toMutableList())
    }
}
