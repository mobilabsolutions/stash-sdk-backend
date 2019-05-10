package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Alias
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

    @Query("SELECT DISTINCT tr FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey AND tr.action = :action AND tr.merchant = :merchant AND (tr.alias = :alias OR :alias IS NULL)")
    fun getByIdempotentKeyAndActionAndMerchantAndAlias(@Param("idempotentKey") idempotentKey: String, @Param("action") action: TransactionAction, @Param("merchant") merchant: Merchant, @Param("alias") alias: Alias?): Transaction?

    @Query("SELECT DISTINCT tr FROM Transaction tr WHERE tr.transactionId = :transactionId AND (tr.action = :action1 OR tr.action = :action2) AND (tr.status = :status OR :status IS NULL)")
    fun getByTransactionIdAndActions(@Param("transactionId") transactionId: String, @Param("action1") action1: TransactionAction, @Param("action2") action2: TransactionAction, @Param("status") status: TransactionStatus? = null): Transaction?

    @Query(value = "SELECT * FROM transaction_record tr WHERE tr.transaction_id = :transactionId AND tr.status = :status GROUP BY :transactionId, tr.id ORDER BY tr.created_date DESC LIMIT 1", nativeQuery = true)
    fun getByTransactionIdAndStatus(@Param("transactionId") transactionId: String, @Param("status") status: String): Transaction?

    @Query(value = "SELECT * FROM transaction_record tr WHERE tr.transaction_id = :transactionId GROUP BY :transactionId, tr.id ORDER BY tr.created_date DESC LIMIT 1", nativeQuery = true)
    fun getByTransactionId(@Param("transactionId") transactionId: String): Transaction?

    @Query("SELECT tr.transaction_id, tr.amount, tr.currency_id, tr.status, tr.action, tr.reason, tr.merchant_customer_id, tr.payment_method, tr.created_date FROM transaction_record tr WHERE tr.merchant_id = :merchantId ORDER BY tr.created_date LIMIT :limit OFFSET :offset", nativeQuery = true)
    fun getTransactionsByLimitAndOffset(@Param("merchantId") merchantId: String, @Param("limit") limit: Int, @Param("offset") offset: Int): List<Array<Any>>
}
