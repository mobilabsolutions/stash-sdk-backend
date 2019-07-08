/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class ScheduledTasks(
    private val notificationService: NotificationService
) {
    companion object : KLogging()

    @Value("\${payment.notification.psp.list:}")
    private lateinit var pspList: String

    @Value("\${payment.notification.psp.list.separator:}")
    private lateinit var pspSeparator: String

    @Value("\${payment.notification.processing.parallelism:}")
    private lateinit var parallelism: String

    @Scheduled(fixedRateString = "\${payment.notification.processing.interval:}")
    fun processNotifications() {
        logger.info { "Notification processing is starting..." }
        pspList.split(pspSeparator).forEach { psp ->
            run {
                try { PaymentServiceProvider.valueOf(psp) } catch (e: IllegalArgumentException) {
                    logger.info { "Unknown PSP '$psp' is defined." }
                    return@forEach }
                repeat(parallelism.toInt()) {
                    GlobalScope.launch(Dispatchers.IO) {
                        notificationService.processNotifications(psp)
                    }
                }
            }
        }
    }
}
