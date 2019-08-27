package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.KeyPerformanceModel
import com.mobilabsolutions.payment.model.LiveTransactionModel
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
        private const val DAY_PATTERN = "EEEE"
        private const val REFUND_NOTIFICATION = "Refunded %s"
        private const val CHARGEBACK_NOTIFICATION = "Chargeback %s"

        private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_UTC)
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
        val transactions = transactionRepository.getTransactionsWithNotification(merchantId, getPastDate(merchant, 1), null)
        val notifications = transactions.map {
            when (it.action) {
                TransactionAction.REFUND -> NotificationModel(it.paymentMethod?.name, REFUND_NOTIFICATION.format("${it.amount}${it.currencyId}"))
                TransactionAction.CHARGEBACK -> NotificationModel(it.paymentMethod?.name, CHARGEBACK_NOTIFICATION.format("${it.amount}${it.currencyId}"))
                else -> null
            }
        }
        return NotificationsResponseModel(notifications, getTransactionsForYesterday(merchant))
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
        val transactions = transactionRepository.getTransactionsForRefunds(merchantId, getPastDate(merchant, 6), null)
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val refundsMap = LinkedHashMap<String, Int>()
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
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern(DAY_PATTERN).withZone(ZoneId.of(timezone)).format(transaction.createdDate)
            if (!transactionsMap.containsKey(day)) transactionsMap[day] = LinkedHashMap()
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
        val startOfDay = dateFormatter.format(LocalDateTime.parse(date, dateFormatter).with(LocalTime.MIN).atZone(ZoneId.of(timezone)))
        val endOfDay = dateFormatter.format(LocalDateTime.parse(date, dateFormatter).with(LocalTime.MAX).atZone(ZoneId.of(timezone)))
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
     * @param transaction Transaction
     * @param merchantId Merchant Id
     * @return live data response model
     */
    @Transactional(readOnly = true)
    fun toLiveData(transaction: LiveTransactionModel, merchantId: String): LiveDataResponseModel? {
        logger.info("Sending the live data for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()

        return when (transaction.action) {
            TransactionAction.AUTH.name -> getLiveDataForAuthAndCapturedTransaction(transaction, merchant)
            TransactionAction.CAPTURE.name -> getLiveDataForAuthAndCapturedTransaction(transaction, merchant)
            TransactionAction.REFUND.name -> getLiveDataForRefundedTransaction(transaction, merchant)
            TransactionAction.CHARGEBACK.name -> getLiveDataForChargedbackTransaction(transaction, merchant)
            TransactionAction.CHARGEBACK_REVERSED.name -> getLiveDataForChargebackReversedTransactions(transaction, merchant)
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
        return dateFormatter
            .withZone(ZoneId.of(merchant.timezone ?: ZoneId.systemDefault().toString()))
            .format(Instant.now().minus(days, ChronoUnit.DAYS))
    }

    private fun initHourlyMap(transactionsMap: LinkedHashMap<String, Int>) {
        for (hour in 0..23) {
            transactionsMap[hour.toString()] = 0
        }
    }

    private fun getLiveDataForAuthAndCapturedTransaction(transaction: LiveTransactionModel, merchant: Merchant): LiveDataResponseModel? {
        return when (transaction.status) {
            TransactionStatus.SUCCESS.name -> LiveDataResponseModel(
                keyPerformance = KeyPerformanceModel(
                    salesVolume = transaction.amount,
                    currencyId = merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 0
                ),
                todaysActivity = TodaysActivityModel(
                    time = getTransactionTime(transaction, merchant),
                    amount = transaction.amount
                ),
                notifications = null
            )
            else -> null
        }
    }

    private fun getLiveDataForRefundedTransaction(transaction: LiveTransactionModel, merchant: Merchant): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = when (transaction.status) {
                TransactionStatus.SUCCESS.name -> KeyPerformanceModel(
                    salesVolume = transaction.amount?.unaryMinus(),
                    currencyId = merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 1,
                    nrOfChargebacks = 0
                )
                else -> null
            },
            todaysActivity = when (transaction.status) {
                TransactionStatus.SUCCESS.name -> TodaysActivityModel(
                    time = getTransactionTime(transaction, merchant),
                    amount = transaction.amount?.unaryMinus()
                )
                else -> null
            },
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod,
                        content = REFUND_NOTIFICATION.format("${transaction.amount}${transaction.currencyId}")
                    ),
                    nrOfransactions = 0
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForChargedbackTransaction(transaction: LiveTransactionModel, merchant: Merchant): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = when (transaction.status) {
                TransactionStatus.SUCCESS.name -> KeyPerformanceModel(
                    salesVolume = transaction.amount?.unaryMinus(),
                    currencyId = merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 1
                )
                else -> null
            },
            todaysActivity = when (transaction.status) {
                TransactionStatus.SUCCESS.name -> TodaysActivityModel(
                    time = getTransactionTime(transaction, merchant),
                    amount = transaction.amount?.unaryMinus()
                )
                else -> null
            },
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod,
                        content = CHARGEBACK_NOTIFICATION.format("${transaction.amount}${transaction.currencyId}")
                    ),
                    nrOfransactions = 0
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForChargebackReversedTransactions(transaction: LiveTransactionModel, merchant: Merchant): LiveDataResponseModel? {
        return when (transaction.status) {
            TransactionStatus.SUCCESS.name -> LiveDataResponseModel(
                keyPerformance = KeyPerformanceModel(
                    salesVolume = transaction.amount,
                    currencyId = merchant.defaultCurrency,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 0
                ),
                todaysActivity = TodaysActivityModel(
                    time = getTransactionTime(transaction, merchant),
                    amount = transaction.amount
                ),
                notifications = null
            )
            else -> null
        }
    }

    private fun getLiveDataForOtherTransactions(transaction: LiveTransactionModel): LiveDataResponseModel? {
        return when (transaction.status) {
            TransactionStatus.SUCCESS.name -> LiveDataResponseModel(
                keyPerformance = KeyPerformanceModel(
                    salesVolume = 0,
                    currencyId = null,
                    nrOfTransactions = 1,
                    nrOfRefundedTransactions = 0,
                    nrOfChargebacks = 0
                ),
                todaysActivity = null,
                notifications = null
            )
            else -> null
        }
    }

    private fun getTransactionTime(transaction: LiveTransactionModel, merchant: Merchant): String {
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val createdDate = LocalDateTime.ofInstant(LocalDateTime.parse(transaction.createdDate).toInstant(ZoneOffset.UTC), ZoneId.of(timezone))
        val hour = createdDate.hour
        val minute = createdDate.minute
        val second = createdDate.second
        return "$hour:$minute:$second"
    }

    private fun getTransactionsForYesterday(merchant: Merchant): Int? {
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val yesterdayBeginOfDay = dateFormatter.format(LocalDateTime.now().minusDays(1).with(LocalTime.MIN).atZone(ZoneId.of(timezone)))
        val yesterdayEndOfDay = dateFormatter.format(LocalDateTime.now().minusDays(1).with(LocalTime.MAX).atZone(ZoneId.of(timezone)))
        val transactions = transactionRepository.getTransactionsByMerchantId(merchant.id!!, yesterdayBeginOfDay, yesterdayEndOfDay)
        return transactions.size
    }
}
