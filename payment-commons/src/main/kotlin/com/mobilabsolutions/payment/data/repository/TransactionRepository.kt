package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.model.PaymentRequestModel
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface TransactionRepository : BaseRepository<Transaction, Long> {

    @Query("SELECT tr.id FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey AND tr.action = :action")
    fun getIdByIdempotentKeyAndAction(@Param("idempotentKey") idempotentKey: String, @Param("action") action: TransactionAction): Long?

    @Query(
        "SELECT tr.id FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey AND tr.action = :action AND tr.alias.id = :#{#paymentInfo.aliasId} AND tr.merchantCustomerId = :#{#paymentInfo.customerId} " +
            "AND tr.merchantTransactionId = :#{#paymentInfo.purchaseId} AND tr.amount = :#{#paymentInfo.paymentData.amount} AND tr.currencyId = :#{#paymentInfo.paymentData.currency} AND tr.reason = :#{#paymentInfo.paymentData.reason}"
    )
    fun getIdByIdempotentKeyAndActionAndGivenBody(
        @Param("idempotentKey") idempotentKey: String,
        @Param("action") action: TransactionAction,
        @Param("paymentInfo") paymentInfo: PaymentRequestModel
    ): Long?

    @Query("SELECT DISTINCT tr FROM Transaction tr WHERE tr.transactionId = :transactionId AND tr.action = :action")
    fun getByTransactionIdAndAction(@Param("transactionId") transactionId: String, @Param("action") action: TransactionAction): Transaction?
}