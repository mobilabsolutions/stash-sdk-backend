package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.repository.MerchantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class ScheduledTasks(
    private val transactionService: TransactionService,
    private val merchantRepository: MerchantRepository
) {
    companion object {
        const val PARALLELISM = "5"
        const val INTERVAL = "5000"
    }

    @Value(PARALLELISM)
    private lateinit var parallelism: String

    @Transactional
    @Scheduled(fixedRateString = INTERVAL)
    fun processNotifications() {
        val merchants = merchantRepository.getMerchantsByWebhookUrl()

        merchants.forEach { merchant ->
            run {
                repeat(parallelism.toInt()) {
                    GlobalScope.launch(Dispatchers.IO) {
                        transactionService.processNotifications(merchant.id!!)
                    }
                }
            }
        }
    }
}
