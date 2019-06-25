package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.TransactionAction
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

    @Query("SELECT * FROM transaction_record tr WHERE tr.transaction_id = :transactionId AND tr.status = :status GROUP BY :transactionId, tr.id ORDER BY tr.created_date DESC LIMIT 1", nativeQuery = true)
    fun getByTransactionIdAndStatus(@Param("transactionId") transactionId: String, @Param("status") status: String): Transaction?

    @Query("SELECT * FROM transaction_record tr WHERE tr.transaction_id = :transactionId AND tr.status = :status", nativeQuery = true)
    fun getListByTransactionIdAndStatus(@Param("transactionId") transactionId: String, @Param("status") status: String): MutableList<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.transaction_id = :transactionId GROUP BY :transactionId, tr.id ORDER BY tr.created_date DESC LIMIT 1", nativeQuery = true)
    fun getByTransactionId(@Param("transactionId") transactionId: String): Transaction?

    @Query("SELECT tr.amount, tr.reason, tr.action, tr.status, tr.created_date FROM transaction_record tr WHERE tr.transaction_id = :transactionId ORDER BY tr.created_date desc", nativeQuery = true)
    fun getTransactionDetails(@Param("transactionId") transactionId: String): List<Array<Any>>

    @Query(
        "SELECT tr.transaction_id, tr.amount, tr.currency_id, tr.status, tr.action, tr.reason, tr.merchant_customer_id, " +
            "COALESCE(CAST(tr.payment_info AS json)#>>'{extra, ccConfig, ccType}', tr.payment_method), tr.created_date, count(*) OVER() AS full_count FROM transaction_record tr " +
            "JOIN (" +
            "SELECT transaction_id, max(created_date) max_created_date " +
            "FROM transaction_record " +
            "GROUP BY transaction_id" +
            ") tr1 ON tr.transaction_id = tr1.transaction_id AND tr.created_date = tr1.max_created_date " +
            "WHERE tr.merchant_id = :merchantId " +
            "AND tr.payment_method = CASE WHEN :paymentMethod <> '' THEN CAST(:paymentMethod AS varchar) ELSE tr.payment_method END " +
            "AND tr.action = CASE WHEN :action <> '' THEN CAST(:action AS varchar) ELSE tr.action END " +
            "AND tr.status = CASE WHEN :status <> '' THEN CAST(:status AS varchar) ELSE tr.status END " +
            "AND tr.created_date >= CASE WHEN :createdAtStart <> '' THEN TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
            "AND tr.created_date <= CASE WHEN :createdAtEnd <> '' THEN TO_TIMESTAMP(CAST(:createdAtEnd AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
            "AND CASE WHEN :text <> '' THEN (CAST(tr.payment_info AS json)#>>'{pspConfig, type}' ~* CAST(:text AS text) " +
            "OR tr.reason ~* CAST(:text AS varchar) " +
            "OR tr.transaction_id ~* CAST(:text AS varchar) " +
            "OR tr.currency_id ~* CAST(:text AS varchar) " +
            "OR tr.merchant_transaction_id ~* CAST(:text AS varchar) " +
            "OR tr.merchant_customer_id ~* CAST(:text AS varchar) " +
            "OR tr.alias_id ~* CAST(:text AS varchar) " +
            "OR tr.payment_method ~* CASE WHEN CAST(:text AS varchar) ~* 'pay' THEN 'PAY_PAL' WHEN CAST(:text AS varchar) ~* 'credit' THEN 'CC' ELSE CAST(:text AS varchar) END " +
            "OR CAST(tr.payment_info AS json)#>>'{extra, ccConfig, ccMask}' ~* CAST(:text AS text) " +
            "OR CAST(tr.payment_info AS json)#>>'{extra, ccConfig, ccExpiryDate}' ~* CAST(:text AS text) " +
            "OR CAST(tr.payment_info AS json)#>>'{extra, ccConfig, ccType}' ~* CAST(:text AS text) " +
            "OR CAST(tr.payment_info AS json)#>>'{extra, sepaConfig, iban}' ~* CAST(:text AS text)) ELSE tr.reason = tr.reason END " +
            "ORDER BY tr.created_date desc LIMIT :limit OFFSET :offset",
        nativeQuery = true)
    fun getTransactionsByFilters(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String?,
        @Param("createdAtEnd") createdAtEnd: String?,
        @Param("paymentMethod") paymentMethod: String?,
        @Param("action") action: String?,
        @Param("status") status: String?,
        @Param("text") text: String?,
        @Param("limit") limit: Int?,
        @Param("offset") offset: Int?
    ): List<Array<Any>>
}
