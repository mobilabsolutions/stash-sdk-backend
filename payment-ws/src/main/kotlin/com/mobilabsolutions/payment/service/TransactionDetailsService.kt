package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.TransactionReportModel
import com.mobilabsolutions.payment.model.TransactionTimelineModel
import com.mobilabsolutions.payment.model.response.TransactionDetailsResponseModel
import com.mobilabsolutions.payment.model.response.TransactionListResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.supercsv.io.CsvBeanWriter
import org.supercsv.prefs.CsvPreference
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletResponse

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
@Transactional
class TransactionDetailsService(
    private val transactionRepository: TransactionRepository,
    private val merchantRepository: MerchantRepository,
    private val objectMapper: ObjectMapper
) {

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
        const val PREAUTHORIZED = "Pre-authorised"
        const val AUTHORIZED = "Authorised"
        const val CAPTURED = "Captured"
        const val REVERSED = "Reversed"
        const val REFUNDED = "Refunded"
        const val FAILED = "Failed"

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(ZoneId.of("Europe/Berlin"))
        val csvHeaders = arrayOf("no", "id", "amount", "currency", "status", "reason", "customerId", "paymentMethod", "createdDate")
    }

    /**
     * Get transaction by ID
     *
     * @param merchantId Merchant ID
     * @param transactionId Transaction ID
     * @return transaction details by id response
     */
    fun getTransactionDetails(merchantId: String, transactionId: String): TransactionDetailsResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transaction = transactionRepository.getByTransactionId(transactionId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTION_NOT_FOUND).asException()
        val timelineTransactions = transactionRepository.getTransactionDetails(transactionId)

        return TransactionDetailsResponseModel(
            dateTimeFormatter.format(transaction.createdDate),
            transaction.transactionId,
            transaction.currencyId,
            transaction.amount,
            transaction.reason,
            transaction.action!!.name,
            transaction.status!!.name,
            transaction.paymentMethod!!.name,
            objectMapper.readValue(transaction.paymentInfo, PaymentInfoModel::class.java),
            transaction.merchantTransactionId,
            transaction.merchantCustomerId,
            transaction.pspTestMode,
            transaction.merchant.id,
            transaction.alias!!.id,
            timelineTransactions.asSequence().map { TransactionTimelineModel(it) }.toMutableList()
        )
    }

    /**
     * Get transactions by given filters
     *
     * @param merchantId
     * @param createdAtStart created date start
     * @param createdAtEnd created date end
     * @param paymentMethod payment method
     * @param action action
     * @param status status
     * @param text any transaction related information
     * @param limit requested transaction list limit
     * @param offset requested transaction list offset
     * @return filtered transaction list
     */
    fun getTransactionsByFilters(
        merchantId: String,
        createdAtStart: String?,
        createdAtEnd: String?,
        paymentMethod: String?,
        action: String?,
        status: String?,
        text: String?,
        limit: Int?,
        offset: Int?
    ): TransactionListResponseModel {
        merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsByFilters(merchantId, createdAtStart, createdAtEnd, paymentMethod,
            action, status, text, limit, offset)

        val transactionList = TransactionListResponseModel(transactions, offset, limit)
        if (transactionList.transactions.isEmpty()) throw ApiError.ofErrorCode(ApiErrorCode.TRANSACTIONS_NOT_FOUND).asException()
        return transactionList
    }

    /**
     * Writes the transaction list to csv file
     *
     * @param response
     * @param merchantId
     * @param createdAtStart created date start
     * @param createdAtEnd created date end
     * @param paymentMethod payment method
     * @param action action
     * @param status status
     * @param text any transaction related information
     * @param limit requested transaction list limit
     * @param offset requested transaction list offset
     */
    fun writeTransactionsToCsv(
        response: HttpServletResponse,
        merchantId: String,
        createdAtStart: String?,
        createdAtEnd: String?,
        paymentMethod: String?,
        action: String?,
        status: String?,
        text: String?,
        limit: Int?,
        offset: Int?
    ) {
        validateSearchPeriod(createdAtStart, createdAtEnd)
        val csvWriter = CsvBeanWriter(response.writer, CsvPreference.STANDARD_PREFERENCE)
        csvWriter.writeHeader(*csvHeaders)
        val transactionList = getTransactionsByFilters(merchantId, createdAtStart, createdAtEnd, paymentMethod, action, status, text, limit, offset)
        for (transaction in transactionList.transactions) {
            val transactions = TransactionReportModel(
                csvWriter.lineNumber,
                transaction.transactionId,
                transaction.amount?.toDouble()?.div(100),
                transaction.currencyId,
                mapStatus(transaction.status, transaction.action),
                transaction.reason,
                transaction.customerId,
                transaction.paymentMethod,
                transaction.createdDate
            )
            csvWriter.write(transactions, *csvHeaders)
        }
        csvWriter.close()
    }

    private fun validateSearchPeriod(createdAtStart: String?, createdAtEnd: String?) {
        val simpleDateFormat = SimpleDateFormat(DATE_PATTERN)
        val startDate = if (createdAtEnd != null) simpleDateFormat.parse(createdAtStart) else Date()
        val endDate = if (createdAtEnd != null) simpleDateFormat.parse(createdAtEnd) else Date()
        val diffInMillis = Math.abs(endDate.time - startDate.time)
        val diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
        if (diff > 366) throw ApiError.ofErrorCode(ApiErrorCode.EXCEEDED_MAX_TRANSACTION_SEARCH_PERIOD).asException()
    }

    private fun mapStatus(status: String?, action: String?): String {
        return when (true) {
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.PREAUTH.name -> PREAUTHORIZED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.AUTH.name -> AUTHORIZED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.CAPTURE.name -> CAPTURED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.REVERSAL.name -> REVERSED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.REFUND.name -> REFUNDED
            else -> FAILED
        }
    }
}
