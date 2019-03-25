package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.model.AuthorizeRequestModel
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface TransactionRepository : BaseRepository<Transaction, Long> {

    @Query("SELECT tr.id FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey")
    fun getIdByIdempotentKey(@Param("idempotentKey") idempotentKey: String): Long?

    @Query("SELECT tr.id FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey AND tr.alias.id = :#{#authorizeInfo.aliasId} AND tr.merchantCustomerId = :#{#authorizeInfo.customerId} " +
        "AND tr.merchantTransactionId = :#{#authorizeInfo.purchaseId} AND tr.amount = :#{#authorizeInfo.paymentData.amount} AND tr.currencyId = :#{#authorizeInfo.paymentData.currency} AND tr.reason = :#{#authorizeInfo.paymentData.reason}")
    fun getIdByIdempotentKeyAndGivenBody(@Param("idempotentKey") idempotentKey: String, @Param("authorizeInfo") authorizeInfo: AuthorizeRequestModel): Long?
}