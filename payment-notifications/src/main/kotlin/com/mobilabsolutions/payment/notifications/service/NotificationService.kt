/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.adyen.model.request.AdyenNotificationRequestModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenNotificationResponseModel
import com.mobilabsolutions.payment.data.enum.NotificationStatus
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.notifications.data.Notification
import com.mobilabsolutions.payment.notifications.data.NotificationId
import com.mobilabsolutions.payment.notifications.data.repository.NotificationRepository
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val pspRegistry: PspRegistry,
    private val notificationClient: NotificationClient,
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    @Value("\${payment.ws.notification.url:}")
    private lateinit var paymentURL: String

    @Value("\${payment.ws.notification.apiKey:}")
    private lateinit var paymentApiKey: String

    /**
     * Saves Adyen notifications and returns confirmation message
     *
     * @param adyenNotificationRequestModel Adyen notification request model
     * @return Adyen notification response model
     */
    @Transactional
    fun saveAdyenNotifications(adyenNotificationRequestModel: AdyenNotificationRequestModel?): AdyenNotificationResponseModel {
        logger.info(
            "Adding Adyen transaction notifications for references ${adyenNotificationRequestModel?.notificationItems?.stream()?.map { it.notificationRequestItem?.pspReference }?.collect(
                Collectors.joining(",")
            )}"
        )
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

    /**
     * Processes transaction notifications and updates their statuses
     *
     * @param psp Payment service provider
     */
    @Transactional
    fun processNotifications(psp: String) {
        logger.info("Picking notifications for $psp")
        val pspImpl = pspRegistry.find(PaymentServiceProvider.valueOf(psp))
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '$psp' cannot be found").asException()

        val notifications = notificationRepository.findNotificationByPsp(psp, 2)

        notifications.forEach {
            logger.info { "Processing PSP transaction ${it.notificationId.pspTransactionId} and status ${it.status}" }
        }

        val notificationModels = notifications.stream().map {
            pspImpl.getPspNotification(it.notificationId.pspTransactionId, it.notificationId.pspEvent, it.message)
        }.collect(Collectors.toList())

        val statusCode = notificationClient.sendNotifications(paymentURL, paymentApiKey, notificationModels)

        notifications.forEach {
            it.status = if (statusCode == HttpStatus.CREATED.value()) NotificationStatus.SUCCESS else NotificationStatus.FAIL
            notificationRepository.save(it)
        }
    }
}
