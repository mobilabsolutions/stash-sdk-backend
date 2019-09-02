package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
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
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
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
    private val merchantUserRepository: MerchantUserRepository,
    private val merchantRepository: MerchantRepository,
    private val simpleMessagingTemplate: SimpMessagingTemplate
) {

    companion object : KLogging() {
        private const val DATE_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        private const val DAY_PATTERN = "EEEE"
        private const val REFUND_NOTIFICATION = "refund of %s"
        private const val CHARGEBACK_NOTIFICATION = "Chargeback %s"

        private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_UTC)
    }

    /**
     * Sends the live data to the client (Web dashboard) via WebSocket
     *
     * @param transaction successful transaction
     */
    @KafkaListener(topics = ["\${kafka.transactions.topicName:}"], groupId = "\${spring.kafka.consumer.group-id:}")
    fun getLiveData(@Payload transaction: Transaction) {
        logger.info { "Started listening the live data" }
        val merchantUsers = merchantUserRepository.getMerchantUsers(transaction.merchant.id!!)
        merchantUsers.forEach { user ->
            simpleMessagingTemplate.convertAndSendToUser(user.email, "/topic/transactions", toLiveData(transaction))
        }
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
                        TransactionStatus.SUCCESS -> NotificationModel(it.paymentMethod?.name, "Successful " + REFUND_NOTIFICATION.format("${it.amount!!.toDouble().div(100)}${it.currencyId}"))
                        TransactionStatus.FAIL -> NotificationModel(it.paymentMethod?.name, "Failed " + REFUND_NOTIFICATION.format("${it.amount!!.toDouble().div(100)}${it.currencyId}"))
                        else -> null
                    }
                TransactionAction.CHARGEBACK -> NotificationModel(it.paymentMethod?.name, CHARGEBACK_NOTIFICATION.format("${it.amount}${it.currencyId}"))
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

    private fun toLiveData(transaction: Transaction): LiveDataResponseModel {
        return when (transaction.action) {
            TransactionAction.AUTH -> getLiveDataForAuthAndCapturedTransaction(transaction)
            TransactionAction.CAPTURE -> getLiveDataForAuthAndCapturedTransaction(transaction)
            TransactionAction.REFUND -> getLiveDataForRefundedTransaction(transaction)
            TransactionAction.CHARGEBACK -> getLiveDataForChargedbackTransaction(transaction)
            TransactionAction.CHARGEBACK_REVERSED -> getLiveDataForChargebackReversedTransactions(transaction)
            else -> getLiveDataForOtherTransactions()
        }
    }

    private fun getLiveDataForAuthAndCapturedTransaction(transaction: Transaction): LiveDataResponseModel {
        return LiveDataResponseModel(
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
    }

    private fun getLiveDataForRefundedTransaction(transaction: Transaction): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = KeyPerformanceModel(
                salesVolume = transaction.amount?.unaryMinus(),
                currencyId = transaction.merchant.defaultCurrency,
                nrOfTransactions = 1,
                nrOfRefundedTransactions = 1,
                nrOfChargebacks = 0
            ),
            todaysActivity = TodaysActivityModel(
                time = getTransactionTime(transaction),
                amount = transaction.amount?.unaryMinus()
            ),
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod?.name,
                        content = REFUND_NOTIFICATION.format("${transaction.amount}${transaction.currencyId}")
                    ),
                    nrOfTransactions = 1
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForChargedbackTransaction(transaction: Transaction): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = KeyPerformanceModel(
                salesVolume = transaction.amount?.unaryMinus(),
                currencyId = transaction.merchant.defaultCurrency,
                nrOfTransactions = 1,
                nrOfRefundedTransactions = 0,
                nrOfChargebacks = 1
            ),
            todaysActivity = TodaysActivityModel(
                time = getTransactionTime(transaction),
                amount = transaction.amount?.unaryMinus()
            ),
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod?.name,
                        content = CHARGEBACK_NOTIFICATION.format("${transaction.amount}${transaction.currencyId}")
                    ),
                    nrOfTransactions = 1
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForChargebackReversedTransactions(transaction: Transaction): LiveDataResponseModel {
        return LiveDataResponseModel(
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
    }

    private fun getLiveDataForOtherTransactions(): LiveDataResponseModel {
        return LiveDataResponseModel(
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
    }

    private fun getTransactionTime(transaction: Transaction): String {
        val timezone = transaction.merchant.timezone ?: ZoneId.systemDefault().toString()
        val hour = LocalDateTime.ofInstant(transaction.createdDate, ZoneId.of(timezone)).hour
        val minute = LocalDateTime.ofInstant(transaction.createdDate, ZoneId.of(timezone)).minute
        val second = LocalDateTime.ofInstant(transaction.createdDate, ZoneId.of(timezone)).second
        return "$hour:$minute:$second"
    }

    private fun getTransactionsForLastWeek(merchant: Merchant, transactionsMap: LinkedHashMap<String, Int>): LinkedHashMap<String, Int> {
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val startDate = dateFormatter.format(LocalDateTime.now().minusDays(7).with(LocalTime.MIN).atZone(ZoneId.of(timezone)))
        val endDate = dateFormatter.format(LocalDateTime.now().with(LocalTime.MIN).atZone(ZoneId.of(timezone)))
        val transactions = transactionRepository.getTransactionsByMerchantId(merchant.id!!, startDate, endDate)
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern(DAY_PATTERN).withZone(ZoneId.of(timezone)).format(transaction.createdDate)
            transactionsMap.computeIfPresent(day) { _, v -> v + 1 }
        }
        return transactionsMap
    }
}
