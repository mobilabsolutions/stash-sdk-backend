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

    @Query("SELECT * FROM transaction_record tr WHERE tr.transaction_id = :transactionId AND tr.status = :status GROUP BY :transactionId, tr.id ORDER BY tr.created_date DESC LIMIT 1", nativeQuery = true)
    fun getByTransactionIdAndStatus(@Param("transactionId") transactionId: String, @Param("status") status: String): Transaction?

    @Query("SELECT * FROM transaction_record tr WHERE tr.transaction_id = :transactionId GROUP BY :transactionId, tr.id ORDER BY tr.created_date DESC LIMIT 1", nativeQuery = true)
    fun getByTransactionId(@Param("transactionId") transactionId: String): Transaction?

    @Query(
        "SELECT tr1.transaction_id, tr1.amount, tr1.currency_id, tr1.status, tr1.action, tr1.reason, tr1.merchant_customer_id, tr1.payment_method, tr1.created_date FROM transaction_record tr1\n" +
            "JOIN (\n" +
            "    SELECT transaction_id, max(created_date) max_created_date\n" +
            "    FROM transaction_record\n" +
            "    GROUP BY transaction_id\n" +
            ") tr2 ON tr1.transaction_id = tr2.transaction_id AND tr1.created_date = tr2.max_created_date WHERE tr1.merchant_id = :merchantId ORDER BY tr1.created_date desc LIMIT :limit OFFSET :offset",
        nativeQuery = true
    )
    fun getTransactionsByLimitAndOffset(@Param("merchantId") merchantId: String, @Param("limit") limit: Int, @Param("offset") offset: Int): List<Array<Any>>

    @Query("SELECT tr.transaction_id, tr.currency_id, tr.amount, tr.reason, tr.action, tr.status, tr.payment_method, tr.payment_info, tr.merchant_transaction_id, tr.merchant_customer_id, tr.psp_test_mode, tr.merchant_id, tr.alias_id FROM transaction_record tr WHERE tr.transaction_id = :transactionId", nativeQuery = true)
    fun getTransactionsByTransactionId(@Param("transactionId") transactionId: String): List<Array<Any>>
}
