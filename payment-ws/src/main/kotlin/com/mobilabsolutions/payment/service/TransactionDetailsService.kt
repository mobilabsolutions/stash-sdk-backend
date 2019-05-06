package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.response.TransactionDetailsByIdResponseModel
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
    private val objectMapper: ObjectMapper
) {

    /**
     * Get transaction by ID
     *
     * @param transactionId Transaction ID
     * @return transaction details by id response
     */
    fun getTransaction(transactionId: String): TransactionDetailsByIdResponseModel {
        val transaction = transactionRepository.getById(transactionId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_FOUND).asException()

        return TransactionDetailsByIdResponseModel(
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
}
