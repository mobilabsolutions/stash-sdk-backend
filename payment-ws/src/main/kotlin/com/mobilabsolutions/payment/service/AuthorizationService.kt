package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.AuthorizeRequestModel
import com.mobilabsolutions.payment.model.AuthorizeResponseModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
@Transactional
class AuthorizationService(
    private val transactionRepository: TransactionRepository,
    private val merchantRepository: MerchantRepository
) {
    fun authorize(secretKey: String, idempotentKey: String, authorizeInfo: AuthorizeRequestModel): AuthorizeResponseModel {
        /**
         * TO-DO: Implement authorization with PSP
         */
        val transaction = Transaction(
                amount = authorizeInfo.paymentData.amount,
                currencyId = authorizeInfo.paymentData.currency,
                reason = authorizeInfo.paymentData.reason,
                merchantTransactionId = authorizeInfo.purchaseId,
                merchantCustomerId = authorizeInfo.customerId,
                idempotentKey = idempotentKey,
                merchant = merchantRepository.getMerchantById(authorizeInfo.customerId)
        )
        transactionRepository.save(transaction)

        return AuthorizeResponseModel(transaction.transactionId, authorizeInfo.paymentData.amount,
                authorizeInfo.paymentData.currency, TransactionStatus.SUCCESS, TransactionAction.AUTH)
    }
}