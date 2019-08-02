package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.response.RefundOverviewResponseModel
import com.mobilabsolutions.payment.model.response.PaymentMethodsOverviewResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.HashMap

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class HomeService(
    private val merchantRepository: MerchantRepository,
    private val transactionRepository: TransactionRepository
) {

    /**
     * Gets total amount of refunded transactions on each day for the last 7 days
     *
     * @param merchantId Merchant ID
     * @return Refund overview response model
     */
    @Transactional
    fun getRefundsOverview(merchantId: String): RefundOverviewResponseModel {
        MerchantService.logger.info("Getting refunded transactions for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsForRefunds(merchantId, getPastDate(6), null)
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val refundsMap = HashMap<String, Int>()
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern("EEEE").withZone(ZoneId.of(timezone)).format(transaction.createdDate)
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
    @Transactional
    fun getPaymentMethodsOverview(merchantId: String): PaymentMethodsOverviewResponseModel {
        MerchantService.logger.info("Getting payment methods transactions for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsForPaymentMethods(merchantId, getPastDate(6), null)
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val transactionsMap = HashMap<String, HashMap<String, Int>>()
        for (transaction in transactions) {
            val day = DateTimeFormatter.ofPattern("EEEE").withZone(ZoneId.of(timezone)).format(transaction.createdDate)
            if (!transactionsMap.containsKey(day)) transactionsMap.put(day, HashMap<String, Int>())
            val amount = transactionsMap[day]!![transaction.paymentMethod!!.name] ?: 0
            transactionsMap[day]!!.put(transaction.paymentMethod!!.name, amount + transaction.amount!!)
        }
        return PaymentMethodsOverviewResponseModel(transactionsMap)
    }

    /**
     * Calculates the date in the past given number of days
     *
     * @param days Number of days to subtract
     * @return Date
     */
    fun getPastDate(days: Long): String {
        return LocalDateTime.now().minusDays(days).toString()
    }
}
