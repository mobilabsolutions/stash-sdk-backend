package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface TransactionRepository : BaseRepository<Transaction, Long> {

    @Query("SELECT DISTINCT tr FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey AND tr.action = :action AND tr.merchant = :merchant")
    fun getByIdempotentKeyAndActionAndMerchant(@Param("idempotentKey") idempotentKey: String, @Param("action") action: TransactionAction, merchant: Merchant): Transaction?

    @Query("SELECT DISTINCT tr FROM Transaction tr WHERE tr.transactionId = :transactionId AND tr.action = :action AND (tr.status = :status OR :status IS NULL)")
    fun getByTransactionIdAndAction(@Param("transactionId") transactionId: String, @Param("action") action: TransactionAction, @Param("status") status: TransactionStatus? = null): Transaction?
}
