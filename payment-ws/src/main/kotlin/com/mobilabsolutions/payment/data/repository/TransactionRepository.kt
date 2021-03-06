/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.configuration.BaseRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface TransactionRepository : BaseRepository<Transaction, Long> {
    fun getTransactionById(id: Long): Transaction

    @Query("SELECT DISTINCT tr FROM Transaction tr WHERE tr.idempotentKey = :idempotentKey AND tr.merchant = :merchant")
    fun getByIdempotentKeyAndMerchant(@Param("idempotentKey") idempotentKey: String, @Param("merchant") merchant: Merchant): Transaction?

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
            "FROM transaction_record WHERE action != 'ADDITIONAL'" +
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

    @Query("SELECT * FROM transaction_record tr WHERE CAST(tr.psp_response AS json)#>>'{pspTransactionId}' = :pspTransactionId AND (tr.action = :action1 OR tr.action = :action2) ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
    fun getByPspReferenceAndActions(@Param("pspTransactionId") pspTransactionId: String, @Param("action1") action1: String, @Param("action2") action2: String): Transaction?

    @Query("SELECT * FROM transaction_record tr WHERE CAST(tr.psp_response AS json)#>>'{pspTransactionId}' = :pspTransactionId ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
    fun getByPspReference(@Param("pspTransactionId") pspTransactionId: String): Transaction?

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId AND tr.status = 'SUCCESS' " +
        "AND tr.created_date >= TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') " +
        "AND tr.created_date <= CASE WHEN :createdAtEnd <> '' THEN TO_TIMESTAMP(CAST(:createdAtEnd AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END",
        nativeQuery = true)
    fun getTransactionsByMerchantId(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String,
        @Param("createdAtEnd") createdAtEnd: String?
    ): List<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId AND tr.notification = true " +
        "AND tr.created_date >= TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') " +
        "AND tr.created_date <= CASE WHEN :createdAtEnd <> '' THEN TO_TIMESTAMP(CAST(:createdAtEnd AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END",
        nativeQuery = true)
    fun getTransactionsWithNotification(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String,
        @Param("createdAtEnd") createdAtEnd: String?
    ): List<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId AND tr.status = 'SUCCESS' AND tr.action = 'REFUND'" +
        " AND tr.created_date >= CASE WHEN :createdAtStart <> '' THEN TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
        "ORDER BY tr.created_date",
        nativeQuery = true)
    fun getTransactionsForRefunds(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String?
    ): List<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId AND tr.status = 'SUCCESS' AND (tr.action = 'AUTH'  OR tr.action = 'CAPTURE')" +
        " AND tr.created_date >= CASE WHEN :createdAtStart <> '' THEN TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
        "AND tr.created_date <= CASE WHEN :createdAtEnd <> '' THEN TO_TIMESTAMP(CAST(:createdAtEnd AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
        "ORDER BY tr.created_date",
        nativeQuery = true)
    fun getTransactionsForPaymentMethods(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String?,
        @Param("createdAtEnd") createdAtEnd: String?
    ): List<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId AND tr.processed_notification = FALSE ORDER BY tr.created_date FOR UPDATE OF tr SKIP LOCKED", nativeQuery = true)
    fun getTransactionsByUnprocessedNotifications(@Param("merchantId") merchantId: String): List<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId AND tr.status = 'SUCCESS' AND tr.action = 'CHARGEBACK'" +
        " AND tr.created_date >= CASE WHEN :createdAtStart <> '' THEN TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
        "ORDER BY tr.created_date",
        nativeQuery = true)
    fun getTransactionsForChargebacks(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String?
    ): List<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId" +
        " AND tr.created_date >= CASE WHEN :createdAtStart <> '' THEN TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
        "ORDER BY tr.created_date",
        nativeQuery = true)
    fun getTransactionsOverview(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String?
    ): List<Transaction>

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId AND tr.transaction_id = :transactionId AND tr.status = 'SUCCESS' AND (tr.action = 'AUTH'  OR tr.action = 'PREAUTH')", nativeQuery = true)
    fun getOriginalTransaction(
        @Param("merchantId") merchantId: String,
        @Param("transactionId") transactionId: String
    ): Transaction?

    @Query("SELECT * FROM transaction_record tr WHERE tr.merchant_id = :merchantId " +
        "AND tr.status = CASE WHEN :status <> '' THEN CAST(:status AS varchar) ELSE tr.status END " +
        "AND tr.payment_method = CASE WHEN :paymentMethod <> '' THEN CAST(:paymentMethod AS varchar) ELSE tr.payment_method END " +
        "AND tr.created_date >= CASE WHEN :createdAtStart <> '' THEN TO_TIMESTAMP(CAST(:createdAtStart AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
        "AND tr.created_date <= CASE WHEN :createdAtEnd <> '' THEN TO_TIMESTAMP(CAST(:createdAtEnd AS text), 'yyyy-MM-dd HH24:MI:SS') ELSE tr.created_date END " +
        "AND tr.currency_id = CASE WHEN :currency <> '' THEN CAST(:currency AS varchar) ELSE tr.currency_id END " +
        "AND tr.amount = CASE WHEN :amount <> '' THEN CAST(CAST(CAST(:amount AS varchar) AS float) AS integer) * 100 ELSE tr.amount END " +
        "AND tr.merchant_customer_id = CASE WHEN :customerId <> '' THEN CAST(:customerId AS varchar) ELSE tr.merchant_customer_id END " +
        "AND tr.transaction_id = CASE WHEN :transactionId <> '' THEN CAST(:transactionId AS varchar) ELSE tr.transaction_id END " +
        "AND tr.merchant_transaction_id = CASE WHEN :merchantTransactionId <> '' THEN CAST(:merchantTransactionId AS varchar) ELSE tr.merchant_transaction_id END " +
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
        "ORDER BY tr.created_date",
        nativeQuery = true)
    fun getCustomTransactions(
        @Param("merchantId") merchantId: String,
        @Param("createdAtStart") createdAtStart: String?,
        @Param("createdAtEnd") createdAtEnd: String?,
        @Param("paymentMethod") paymentMethod: String?,
        @Param("status") status: String?,
        @Param("text") text: String?,
        @Param("currency") currency: String?,
        @Param("amount") amount: String?,
        @Param("customerId") customerId: String?,
        @Param("transactionId") transactionId: String?,
        @Param("merchantTransactionId") merchantTransactionId: String?
    ): List<Transaction>
}
