package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.enum.ReportType
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.DashboardReportModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.supercsv.io.CsvBeanWriter
import org.supercsv.prefs.CsvPreference
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.servlet.http.HttpServletResponse

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class ReportService(
    private val transactionRepository: TransactionRepository,
    private val merchantRepository: MerchantRepository
) {

    companion object {
        const val DATE_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_UTC)

        val csvHeaders = arrayOf("no", "id", "initialAmount", "initialCreatedDate", "reason", "customerId", "status", "paymentMethod", "amount", "createdDate")
    }

    /**
     * Download transaction specific reports on the dashboard in CSV format
     *
     * @param response Response
     * @param reportType Report type
     * @param merchantId Merchant ID
     * @param createdAtStart Creation start date
     * @param createdAtEnd Creation end date
     * @param paymentMethod Payment method
     * @param status Status
     * @param text Text
     */
    @Transactional(readOnly = true)
    fun downloadDefaultReports(response: HttpServletResponse, reportType: String?, merchantId: String) {
        HomeService.logger.info("Downloading report of type {} for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()

        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val transactions = when (reportType) {
            ReportType.OVERVIEW.name -> transactionRepository.getTransactionsOverview(merchantId, getPastDate(merchant, 30))
            ReportType.REFUND.name -> transactionRepository.getTransactionsForRefunds(merchantId, getPastDate(merchant, 30))
            else -> transactionRepository.getTransactionsForChargebacks(merchantId, getPastDate(merchant, 30))
        }

        CsvBeanWriter(response.writer, CsvPreference.STANDARD_PREFERENCE).use { csvWriter ->
            csvWriter.writeHeader(*csvHeaders)
            for (transaction in transactions) {
                val originalTransaction = transactionRepository.getOriginalTransaction(merchantId, transaction.transactionId!!)
                val currentTransaction = DashboardReportModel(
                    csvWriter.lineNumber,
                    transaction.transactionId,
                    if (originalTransaction != null) originalTransaction.amount!!.toDouble().div(100).toString() else "-",
                    if (originalTransaction != null) originalTransaction.createdDate!!.atZone(ZoneId.of(timezone)).toString() else "-",
                    transaction.reason,
                    transaction.merchantCustomerId,
                    mapStatus(transaction.status!!.name, transaction.action!!.name),
                    transaction.paymentMethod!!.name,
                    transaction.amount!!.toDouble().div(100).toString(),
                    transaction.createdDate!!.atZone(ZoneId.of(timezone)).toString()
                )
                csvWriter.write(currentTransaction, *csvHeaders)
            }
        }
    }

    /**
     * Calculates the date in the past for the given number of days
     *
     * @param merchant Merchant
     * @param days Number of days to subtract
     * @return date as String
     */
    fun getPastDate(merchant: Merchant, days: Long): String {
        return dateFormatter
            .withZone(ZoneId.of(merchant.timezone ?: ZoneId.systemDefault().toString()))
            .format(Instant.now().minus(days, ChronoUnit.DAYS))
    }

    private fun mapStatus(status: String?, action: String?): String {
        return when {
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.PREAUTH.name -> TransactionDetailsService.PREAUTHORIZED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.AUTH.name -> TransactionDetailsService.AUTHORIZED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.CAPTURE.name -> TransactionDetailsService.CAPTURED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.REVERSAL.name -> TransactionDetailsService.REVERSED
            status == TransactionStatus.SUCCESS.name && action == TransactionAction.REFUND.name -> TransactionDetailsService.REFUNDED
            else -> TransactionDetailsService.FAILED
        }
    }
}
