/*
 * Copyright © MobiLab Solutions GmbH
 */

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
    @Value("\${payment.notification.processing.parallelism:}")
    private lateinit var parallelism: String

    @Transactional
    @Scheduled(fixedRateString = "\${payment.notification.processing.interval:}")
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
