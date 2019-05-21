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

    /**
     * Get transactions by transaction ID
     *
     * @param merchantId Merchant ID
     * @param transactionId Trasanction ID
     * @return transaction list
     */
    fun getTransactionDetails(merchantId: String, transactionId: String): TransactionListResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsByTransactionId(transactionId)

        return TransactionListResponseModel(transactions.asSequence().map { TransactionModel(it) }.toMutableList())
    }
}
