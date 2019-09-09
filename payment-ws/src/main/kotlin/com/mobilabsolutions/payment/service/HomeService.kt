/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.KeyPerformanceModel
import com.mobilabsolutions.payment.model.NotificationModel
import com.mobilabsolutions.payment.model.NotificationsModel
import com.mobilabsolutions.payment.model.TodaysActivityModel
import com.mobilabsolutions.payment.model.response.LiveDataResponseModel
import com.mobilabsolutions.payment.model.response.NotificationsResponseModel
import com.mobilabsolutions.payment.model.response.PaymentMethodsOverviewResponseModel
import com.mobilabsolutions.payment.model.response.RefundOverviewResponseModel
import com.mobilabsolutions.payment.model.response.SelectedDateActivityResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Service
class HomeService(
    private val transactionRepository: TransactionRepository,
    private val merchantRepository: MerchantRepository
) {

    companion object : KLogging() {
        private const val DATE_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        private const val DAY_PATTERN = "EEEE"
        private const val SUCCESSFUL_REFUND_NOTIFICATION = "Refunded %s"
        private const val FAILED_REFUND_NOTIFICATION = "Failed refund of %s"
        private const val CHARGEBACK_NOTIFICATION = "Chargeback %s"

        private val dateFormatterUtc: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_UTC)
        private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
    }

    /**
     * Returns the summary of the activities for the last month
     *
     * @param merchantId Merchant id
     * @return key performance
     */
    @Transactional(readOnly = true)
    fun getKeyPerformance(merchantId: String): KeyPerformanceModel {
        logger.info("Getting key performance for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsByMerchantId(merchantId, getPastDate(merchant, 30), null)
        val salesVolume = transactions.filter { it.action == TransactionAction.AUTH || it.action == TransactionAction.CAPTURE || it.action == TransactionAction.CHARGEBACK_REVERSED }.sumBy { it.amount!! }
        val returnedMoney = transactions.filter { it.action == TransactionAction.REFUND || it.action == TransactionAction.CHARGEBACK }.sumBy { it.amount!! }
        val refundedTransactions = transactions.filter { it.action == TransactionAction.REFUND }.size
        val chargedbackTransactions = transactions.filter { it.action == TransactionAction.CHARGEBACK }.size
        return KeyPerformanceModel(salesVolume - returnedMoney, merchant.defaultCurrency, transactions.size, refundedTransactions, chargedbackTransactions)
    }

    /**
     * Returns the notifications for the last 24h, as well as number of transactions for yesterday
     *
     * @param merchantId Merchant id
     * @return notifications
     */
    @Transactional(readOnly = true)
    fun getNotifications(merchantId: String): NotificationsResponseModel {
        logger.info("Getting notifications for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsWithNotification(merchantId, getPastDate(merchant, 7), null)
        val transactionsMap = LinkedHashMap<String, Int>()
        initNotificationsMap(transactionsMap)
        val notifications = transactions.map {
            when (it.action) {
                TransactionAction.REFUND ->
                    when (it.status) {
                        TransactionStatus.SUCCESS -> NotificationModel(it.paymentMethod?.name, SUCCESSFUL_REFUND_NOTIFICATION.format("${it.amount?.toDouble()?.div(100)}${it.currencyId}"), dateFormatter.withZone(ZoneId.of(merchant.timezone)).format(it.createdDate))
                        TransactionStatus.FAIL -> NotificationModel(it.paymentMethod?.name, FAILED_REFUND_NOTIFICATION.format("${it.amount?.toDouble()?.div(100)}${it.currencyId}"), dateFormatter.withZone(ZoneId.of(merchant.timezone)).format(it.createdDate))
                        else -> null
                    }
                TransactionAction.CHARGEBACK -> NotificationModel(it.paymentMethod?.name, CHARGEBACK_NOTIFICATION.format("${it.amount?.toDouble()?.div(100)}${it.currencyId}"), dateFormatter.withZone(ZoneId.of(merchant.timezone)).format(it.createdDate))
                else -> null
            }
        }
        return NotificationsResponseModel(notifications, getTransactionsForLastWeek(merchant, transactionsMap))
    }

    /**
     * Gets total amount of refunded transactions on each day for the last 7 days
     *
     * @param merchantId Merchant ID
     * @return Refund overview response model
     */
    @Transactional(readOnly = true)
    fun getRefundsOverview(merchantId: String): RefundOverviewResponseModel {
        logger.info("Getting refunded transactions for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsForRefunds(merchantId, getPastDate(merchant, 6))
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val refundsMap = LinkedHashMap<String, Int>()
        initRefundsMap(refundsMap)
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern(DAY_PATTERN).withZone(ZoneId.of(timezone)).format(transaction.createdDate)
            val amount = refundsMap[day] ?: 0
            refundsMap[day] = amount.plus(transaction.amount!!)
        }
        return RefundOverviewResponseModel(refundsMap)
    }

    /**
     * Gets total amount of executed transactions on each day for the last 7 days
     *
     * @param merchantId Merchant ID
     * @return Payment methods overview response model
     */
    @Transactional(readOnly = true)
    fun getPaymentMethodsOverview(merchantId: String): PaymentMethodsOverviewResponseModel {
        MerchantService.logger.info("Getting payment methods transactions for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsForPaymentMethods(merchantId, getPastDate(merchant, 6), null)
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val transactionsMap = LinkedHashMap<String, LinkedHashMap<String, Int>>()
        initDailyMap(transactionsMap)
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern(DAY_PATTERN).withZone(ZoneId.of(timezone)).format(transaction.createdDate)
            val amount = transactionsMap[day]!![transaction.paymentMethod!!.name] ?: 0
            transactionsMap[day]!![transaction.paymentMethod!!.name] = amount + transaction.amount!!
        }
        return PaymentMethodsOverviewResponseModel(transactionsMap)
    }

    /**
     * Gets selected date activity for all captured transactions
     *
     * @param merchantId Merchant ID
     * @param date Date
     *
     * @return Selected date activity response model
     */
    @Transactional(readOnly = true)
    fun getSelectedDateActivity(merchantId: String, date: String?): SelectedDateActivityResponseModel {
        logger.info("Getting selected date's activity for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()

        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val startOfDay = dateFormatterUtc.format(LocalDateTime.parse(date, dateFormatterUtc).with(LocalTime.MIN).atZone(ZoneId.of(timezone)))
        val endOfDay = dateFormatterUtc.format(LocalDateTime.parse(date, dateFormatterUtc).with(LocalTime.MAX).atZone(ZoneId.of(timezone)))
        val transactions = transactionRepository.getTransactionsForPaymentMethods(merchantId, startOfDay, endOfDay)
        val transactionsMap = LinkedHashMap<String, Int>()
        initHourlyMap(transactionsMap)
        for (transaction in transactions) {
            val hour = (transaction.createdDate!!.atZone(ZoneId.of(timezone)).hour).toString()
            val amount = transactionsMap[hour] ?: 0
            transactionsMap[hour] = amount.plus(transaction.amount!!)
        }
        return SelectedDateActivityResponseModel(transactionsMap)
    }

    /**
     * Maps transaction to the live data model
     *
     * @param transactionId Transaction Id
     * @return live data response model
     */
    @Transactional(readOnly = true)
    fun toLiveData(transactionId: Long): LiveDataResponseModel {
        val transaction = transactionRepository.getTransactionById(transactionId)
        logger.info("Sending the live data for merchant {}", transaction.merchant.id)

        return when (transaction.action) {
            TransactionAction.AUTH -> getLiveDataForAuthAndCapturedTransaction(transaction)
            TransactionAction.CAPTURE -> getLiveDataForAuthAndCapturedTransaction(transaction)
            TransactionAction.REFUND -> getLiveDataForRefundedTransaction(transaction)
            TransactionAction.CHARGEBACK -> getLiveDataForChargedbackTransaction(transaction)
            TransactionAction.CHARGEBACK_REVERSED -> getLiveDataForChargebackReversedTransactions(transaction)
            else -> getLiveDataForOtherTransactions(transaction)
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
        return dateFormatterUtc
            .withZone(ZoneId.of(merchant.timezone ?: ZoneId.systemDefault().toString()))
            .format(Instant.now().minus(days, ChronoUnit.DAYS))
    }

    private fun initHourlyMap(transactionsMap: LinkedHashMap<String, Int>) {
        for (hour in 0..23) {
            transactionsMap[hour.toString()] = 0
        }
    }

    private fun initDailyMap(transactionsMap: LinkedHashMap<String, LinkedHashMap<String, Int>>) {
        for (index in 6 downTo 0) {
            transactionsMap[(getCurrentDay() - index.toLong()).getDisplayName(TextStyle.FULL, Locale.getDefault())] = LinkedHashMap()
        }
    }

    private fun initRefundsMap(refundsMap: LinkedHashMap<String, Int>) {
        for (index in 6 downTo 0) {
            refundsMap[(getCurrentDay() - index.toLong()).getDisplayName(TextStyle.FULL, Locale.getDefault())] = 0
        }
    }

    private fun initNotificationsMap(transactionsMap: LinkedHashMap<String, Int>) {
        for (index in 6 downTo 0) {
            transactionsMap[(getCurrentDay() - index.toLong()).getDisplayName(TextStyle.FULL, Locale.getDefault())] = 0
        }
    }

    private fun getCurrentDay(): DayOfWeek {
        return LocalDate.now().dayOfWeek
    }

    private fun getLiveDataForAuthAndCapturedTransaction(transaction: Transaction): LiveDataResponseModel {
        return when (transaction.status) {
            TransactionStatus.SUCCESS -> LiveDataResponseModel(
                keyPerformance = KeyPerformanceModel(
                    salesVolume = transaction.amount,
                    currencyId = transaction.merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 0
                ),
                todaysActivity = TodaysActivityModel(
                    time = getTransactionTime(transaction),
                    amount = transaction.amount
                ),
                notifications = NotificationsModel(
                    notification = null,
                    nrOfTransactions = 1
                )
            )
            else -> LiveDataResponseModel(null, null, null)
        }
    }

    private fun getLiveDataForRefundedTransaction(transaction: Transaction): LiveDataResponseModel {
        val timezone = transaction.merchant.timezone

        return LiveDataResponseModel(
            keyPerformance = when (transaction.status) {
                TransactionStatus.SUCCESS -> KeyPerformanceModel(
                    salesVolume = transaction.amount?.unaryMinus(),
                    currencyId = transaction.merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 1,
                    nrOfChargebacks = 0
                )
                else -> null
            },
            todaysActivity = when (transaction.status) {
                TransactionStatus.SUCCESS -> TodaysActivityModel(
                    time = getTransactionTime(transaction),
                    amount = transaction.amount?.unaryMinus()
                )
                else -> null
            },
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod?.name,
                        content = when (transaction.status) {
                            TransactionStatus.SUCCESS -> SUCCESSFUL_REFUND_NOTIFICATION.format("${transaction.amount?.toDouble()?.div(100)}${transaction.currencyId}")
                            TransactionStatus.FAIL -> FAILED_REFUND_NOTIFICATION.format("${transaction.amount?.toDouble()?.div(100)}${transaction.currencyId}")
                            else -> null
                        },
                        date = dateFormatter.withZone(ZoneId.of(timezone)).format(transaction.createdDate)
                    ),
                    nrOfTransactions = 1
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForChargedbackTransaction(transaction: Transaction): LiveDataResponseModel {
        val timezone = transaction.merchant.timezone

        return LiveDataResponseModel(
            keyPerformance = when (transaction.status) {
                TransactionStatus.SUCCESS -> KeyPerformanceModel(
                    salesVolume = transaction.amount?.unaryMinus(),
                    currencyId = transaction.merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 1
                )
                else -> null
            },
            todaysActivity = when (transaction.status) {
                TransactionStatus.SUCCESS -> TodaysActivityModel(
                    time = getTransactionTime(transaction),
                    amount = transaction.amount?.unaryMinus()
                )
                else -> null
            },
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod?.name,
                        content = CHARGEBACK_NOTIFICATION.format("${transaction.amount?.toDouble()?.div(100)}${transaction.currencyId}"),
                        date = dateFormatter.withZone(ZoneId.of(timezone)).format(transaction.createdDate)
                    ),
                    nrOfTransactions = 1
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForChargebackReversedTransactions(transaction: Transaction): LiveDataResponseModel {
        return when (transaction.status) {
            TransactionStatus.SUCCESS -> LiveDataResponseModel(
                keyPerformance = KeyPerformanceModel(
                    salesVolume = transaction.amount,
                    currencyId = transaction.merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 0
                ),
                todaysActivity = TodaysActivityModel(
                    time = getTransactionTime(transaction),
                    amount = transaction.amount
                ),
                notifications = NotificationsModel(
                    notification = null,
                    nrOfTransactions = 1
                )
            )
            else -> LiveDataResponseModel(null, null, null)
        }
    }

    private fun getLiveDataForOtherTransactions(transaction: Transaction): LiveDataResponseModel {
        return when (transaction.status) {
            TransactionStatus.SUCCESS -> LiveDataResponseModel(
                keyPerformance = KeyPerformanceModel(
                    salesVolume = 0,
                    currencyId = null,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 0
                ),
                todaysActivity = null,
                notifications = NotificationsModel(
                    notification = null,
                    nrOfTransactions = 1
                )
            )
            else -> LiveDataResponseModel(null, null, null)
        }
    }

    private fun getTransactionTime(transaction: Transaction): String {
        val timezone = transaction.merchant.timezone ?: ZoneId.systemDefault().toString()
        val createdDate = LocalDateTime.ofInstant(transaction.createdDate, ZoneId.of(timezone))
        val hour = createdDate.hour
        val minute = createdDate.minute
        val second = createdDate.second
        return "$hour:$minute:$second"
    }

    private fun getTransactionsForLastWeek(merchant: Merchant, transactionsMap: LinkedHashMap<String, Int>): LinkedHashMap<String, Int> {
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val startDate = dateFormatterUtc.format(LocalDateTime.now().minusDays(7).with(LocalTime.MIN).atZone(ZoneId.of(timezone)))
        val endDate = dateFormatterUtc.format(LocalDateTime.now().with(LocalTime.MIN).atZone(ZoneId.of(timezone)))
        val transactions = transactionRepository.getTransactionsByMerchantId(merchant.id!!, startDate, endDate)
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern(DAY_PATTERN).withZone(ZoneId.of(timezone)).format(transaction.createdDate)
            transactionsMap.computeIfPresent(day) { _, v -> v + 1 }
        }
        return transactionsMap
    }
}
