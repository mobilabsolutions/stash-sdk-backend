package com.mobilabsolutions.payment.notifications.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.enum.NotificationStatus
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.notifications.data.Notification
import com.mobilabsolutions.payment.notifications.data.NotificationId
import com.mobilabsolutions.payment.notifications.data.repository.NotificationRepository
import com.mobilabsolutions.payment.notifications.model.request.AdyenNotificationRequestModel
import com.mobilabsolutions.payment.notifications.model.response.AdyenNotificationResponseModel
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    @Transactional
    fun saveAdyenNotifications(adyenNotificationRequestModel: AdyenNotificationRequestModel?): AdyenNotificationResponseModel {
        logger.info("adding Adyen transaction notifications for references ${adyenNotificationRequestModel?.notificationItems?.stream()?.map { it.notificationRequestItem?.pspReference }?.collect(Collectors.joining(","))}")
        adyenNotificationRequestModel?.notificationItems?.forEach {
            notificationRepository.save(Notification(
                notificationId = NotificationId(pspTransactionId = it.notificationRequestItem?.pspReference, pspEvent = it.notificationRequestItem?.eventCode),
                status = NotificationStatus.CREATED,
                psp = PaymentServiceProvider.ADYEN,
                message = objectMapper.writeValueAsString(it.notificationRequestItem)
            ))
        }
        return AdyenNotificationResponseModel(
            notificationResponse = "[accepted]"
        )
    }

    @Transactional
    fun pickNotification(psp: String) {
        logger.info("picking notifications for $psp")
        val notifications = notificationRepository.findNotificationByPsp(psp, 2)

        // process the notification
        // TODO
        notifications.forEach {
            logger.info { "${it.notificationId}, ${it.status}" }
        }

        // send notifications
        // TODO

        // update notifications based on received status
        // TODO
        notifications.forEach {
            it.status = NotificationStatus.SUCCESS
            notificationRepository.save(it)
        }
    }
}
