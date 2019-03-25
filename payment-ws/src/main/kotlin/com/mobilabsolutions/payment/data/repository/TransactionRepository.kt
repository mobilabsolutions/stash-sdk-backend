package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.model.PreauthorizeRequestModel
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

    @Query("SELECT tr.id FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey AND tr.alias.id = :#{#preauthorizeInfo.aliasId} AND tr.merchantCustomerId = :#{#preauthorizeInfo.customerId} " +
        "AND tr.merchantTransactionId = :#{#preauthorizeInfo.purchaseId} AND tr.amount = :#{#preauthorizeInfo.paymentData.amount} AND tr.currencyId = :#{#preauthorizeInfo.paymentData.currency} AND tr.reason = :#{#preauthorizeInfo.paymentData.reason}")
    fun getIdByIdempotentKeyAndGivenBody(@Param("idempotentKey") idempotentKey: String, @Param("preauthorizeInfo") preauthorizeInfo: PreauthorizeRequestModel): Long?
}