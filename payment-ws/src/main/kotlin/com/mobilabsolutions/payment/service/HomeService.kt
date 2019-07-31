package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.response.RefundOverviewResponseModel
import com.mobilabsolutions.payment.model.response.TransactionListResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class HomeService(
    private val merchantRepository: MerchantRepository,
    private val transactionRepository: TransactionRepository
) {

    /**
     * Gets total amount refunded on each day for the last 7 days
     *
     * @param merchantId Merchant ID
     * @return Refund overview response model
     */
    @Transactional
    fun getRefundsOverview(merchantId: String): RefundOverviewResponseModel {
        MerchantService.logger.info("Getting refunded transactions for merchant {}", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val transactions = transactionRepository.getTransactionsByFilters(merchantId, getPastDate(6), null, null,
            TransactionAction.REFUND.name, TransactionStatus.SUCCESS.name, null, 1000, 0)
        val timezone = merchant.timezone ?: ZoneId.systemDefault().toString()
        val transactionList = TransactionListResponseModel(transactions, 0, 1000, timezone)
        val refundsMap = HashMap<String, Int>()
        for (transaction in transactionList.transactions) {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transaction.createdDate!!)
            val day = SimpleDateFormat("EEEE").format(date)
            val amount = refundsMap[day] ?: 0
            refundsMap[day] = amount.plus(transaction.amount!!)
        }
        return RefundOverviewResponseModel(refundsMap)
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
