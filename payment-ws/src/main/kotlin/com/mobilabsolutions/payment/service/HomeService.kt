package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.KeyPerformanceModel
import com.mobilabsolutions.payment.model.NotificationModel
import com.mobilabsolutions.payment.model.NotificationsModel
import com.mobilabsolutions.payment.model.TodaysActivityModel
import com.mobilabsolutions.payment.model.response.LiveDataResponseModel
import com.mobilabsolutions.payment.model.response.NotificationsResponseModel
import com.mobilabsolutions.payment.model.response.RefundOverviewResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

    companion object {
        private const val DATE_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        private const val REFUND_NOTIFICATION = "Refunded %s"
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
        val merchantUsers = merchantUserRepository.getMerchantUsers(transaction.merchant.id!!)
        merchantUsers.forEach { user ->
            simpleMessagingTemplate.convertAndSendToUser(user.email, "/topic/transactions", toLiveData(transaction))
            println("Message sent to ${user.email}")
            println("Message is ${toLiveData(transaction)}")
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
        val merchant = merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getByMerchantId(merchantId, getPastDate(merchant, 30), null)
        val salesVolume = transactions.filter { it.action == TransactionAction.AUTH || it.action == TransactionAction.CAPTURE }.sumBy { it.amount!! }
        val refundedTransactions = transactions.filter { it.action == TransactionAction.REFUND }.size
        val chargedbackTransactions = transactions.filter { it.action == TransactionAction.CHARGEBACK }.size
        return KeyPerformanceModel(salesVolume, transactions.size, refundedTransactions, chargedbackTransactions)
    }

    /**
     * Returns the notifications for the last 24h, as well as number of transactions for yesterday
     *
     * @param merchantId Merchant id
     * @return notifications
     */
    @Transactional(readOnly = true)
    fun getNotifications(merchantId: String): NotificationsResponseModel {
        val merchant = merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getByMerchantId(merchantId, getPastDate(merchant, 1), null)
        val notifications = transactions.filter { it.notification == true }.map {
            when (it.action) {
                TransactionAction.REFUND -> NotificationModel(it.paymentMethod?.name, REFUND_NOTIFICATION.format("${it.amount}${it.currencyId}"))
                TransactionAction.CHARGEBACK -> NotificationModel(it.paymentMethod?.name, CHARGEBACK_NOTIFICATION.format("${it.amount}${it.currencyId}"))
                else -> null
            }
        }
        return NotificationsResponseModel(notifications, getTransactionsForYesterday(merchant))
    }

    /**
     * Gets total amount refunded on each day for the last 7 days
     *
     * @param merchantId Merchant ID
     * @return Refund overview response model
     */
    @Transactional(readOnly = true)
    fun getRefundsOverview(merchantId: String): RefundOverviewResponseModel {
        MerchantService.logger.info("Getting refunded transactions for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsForRefunds(merchantId, getPastDate(merchant,6), null)
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val refundsMap = HashMap<String, Int>()
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern("EEEE").withZone(ZoneId.of(timezone)).format(transaction.createdDate)
            val amount = refundsMap[day] ?: 0
            refundsMap[day] = amount.plus(transaction.amount!!)
        }
        return RefundOverviewResponseModel(refundsMap)
    }

    private fun toLiveData(transaction: Transaction): LiveDataResponseModel {
        return when (transaction.action) {
            TransactionAction.AUTH -> getLiveDataForAuthAndCapturedTransaction(transaction)
            TransactionAction.CAPTURE -> getLiveDataForAuthAndCapturedTransaction(transaction)
            TransactionAction.REFUND -> getLiveDataForRefundedTransaction(transaction)
            TransactionAction.CHARGEBACK -> getLiveDataForChargedbackTransaction(transaction)
            else -> getLiveDataForOtherTransactions()
        }
    }

    private fun getLiveDataForAuthAndCapturedTransaction(transaction: Transaction): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = KeyPerformanceModel(
                salesVolume = transaction.amount,
                transactions = 1,
                refundedTransactions = 0,
                chargebacks = 0
            ),
            todaysActivity = TodaysActivityModel(
                time = getTransactionTime(transaction),
                amount = transaction.amount
            ),
            notifications = null
        )
    }

    private fun getLiveDataForRefundedTransaction(transaction: Transaction): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = KeyPerformanceModel(
                salesVolume = 0,
                transactions = 1,
                refundedTransactions = 1,
                chargebacks = 0
            ),
            todaysActivity = null,
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod?.name,
                        content = REFUND_NOTIFICATION.format("${transaction.amount}${transaction.currencyId}")
                    ),
                    transactions = 0
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForChargedbackTransaction(transaction: Transaction): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = KeyPerformanceModel(
                salesVolume = 0,
                transactions = 1,
                refundedTransactions = 0,
                chargebacks = 1
            ),
            todaysActivity = null,
            notifications = when (transaction.notification) {
                true -> NotificationsModel(
                    notification = NotificationModel(
                        paymentMethod = transaction.paymentMethod?.name,
                        content = CHARGEBACK_NOTIFICATION.format("${transaction.amount}${transaction.currencyId}")
                    ),
                    transactions = 0
                )
                else -> null
            }
        )
    }

    private fun getLiveDataForOtherTransactions(): LiveDataResponseModel {
        return LiveDataResponseModel(
            keyPerformance = KeyPerformanceModel(
                salesVolume = 0,
                transactions = 1,
                refundedTransactions = 0,
                chargebacks = 0
            ),
            todaysActivity = null,
            notifications = null
        )
    }

    private fun getTransactionTime(transaction: Transaction): String {
        val timezone = transaction.merchant.timezone ?: ZoneId.systemDefault().toString()
        val hour = LocalDateTime.ofInstant(transaction.createdDate, ZoneId.of(timezone)).hour
        val minute = LocalDateTime.ofInstant(transaction.createdDate, ZoneId.of(timezone)).minute
        val second = LocalDateTime.ofInstant(transaction.createdDate, ZoneId.of(timezone)).second
        return "$hour:$minute:$second"
    }

    private fun getTransactionsForYesterday(merchant: Merchant): Int? {
        val yesterdayBeginOfDay = dateFormatter.format(
            LocalDateTime.now().minusDays(1).with(LocalTime.MIN).atZone(ZoneId.of(merchant.timezone ?: ZoneId.systemDefault().toString())))
        val yesterdayEndOfDay = dateFormatter.format(
            LocalDateTime.now().minusDays(1).with(LocalTime.MAX).atZone(ZoneId.of(merchant.timezone ?: ZoneId.systemDefault().toString())))
        val transactions = transactionRepository.getByMerchantId(merchant.id!!, yesterdayBeginOfDay, yesterdayEndOfDay)
        return transactions.size
    }

    /**
     * Calculates the date in the past for the given number of days
     *
     * @param days Number of days to subtract
     * @return date as String
     */
    fun getPastDate(merchant: Merchant, days: Long): String {
        return dateFormatter
            .withZone(ZoneId.of(merchant.timezone ?: ZoneId.systemDefault().toString()))
            .format(Instant.now().minus(days, ChronoUnit.DAYS))
    }
}
