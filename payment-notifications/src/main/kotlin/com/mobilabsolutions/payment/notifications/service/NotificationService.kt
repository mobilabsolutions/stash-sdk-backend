/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.enum.NotificationStatus
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.PspNotificationModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PspNotificationListRequestModel
import com.mobilabsolutions.payment.notifications.data.Notification
import com.mobilabsolutions.payment.notifications.data.NotificationId
import com.mobilabsolutions.payment.notifications.data.repository.NotificationRepository
import com.mobilabsolutions.payment.notifications.model.AdyenNotificationItemModel
import com.mobilabsolutions.payment.notifications.model.request.AdyenNotificationRequestModel
import com.mobilabsolutions.payment.notifications.model.response.AdyenNotificationResponseModel
import mu.KLogging
import org.json.JSONObject
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
    private val objectMapper: ObjectMapper,
    @Value("\${payment.ws.notification.url:}")
    private val paymentURL: String,
    @Value("\${payment.ws.notification.apiKey:}")
    private val paymentApiKey: String
) {
    companion object : KLogging()

    @Transactional
    fun saveAdyenNotifications(adyenNotificationRequestModel: AdyenNotificationRequestModel?): AdyenNotificationResponseModel {
        logger.info(
            "adding Adyen transaction notifications for references ${adyenNotificationRequestModel?.notificationItems?.stream()?.map { it.notificationRequestItem?.pspReference }?.collect(
                Collectors.joining(",")
            )}"
        )
        adyenNotificationRequestModel?.notificationItems?.forEach {
            notificationRepository.save(
                Notification(
                    notificationId = NotificationId(
                        pspTransactionId = it.notificationRequestItem?.pspReference,
                        pspEvent = it.notificationRequestItem?.eventCode
                    ),
                    status = NotificationStatus.CREATED,
                    psp = PaymentServiceProvider.ADYEN,
                    message = objectMapper.writeValueAsString(it.notificationRequestItem)
                )
            )
        }
        return AdyenNotificationResponseModel(
            notificationResponse = "[accepted]"
        )
    }

    @Transactional
    fun pickNotification(psp: String) {
        logger.info("picking notifications for $psp")
        val notifications = notificationRepository.findNotificationByPsp(psp, 2)

        notifications.forEach {
            logger.info { "Processing PSP transaction ${it.notificationId.pspTransactionId} and status ${it.status}" }
        }

        val notificationModels = notifications.stream().map {
            PspNotificationModel(
                pspTransactionId = it.notificationId.pspTransactionId,
                paymentData = PaymentDataRequestModel(
                    amount = objectMapper.readValue(it.message, AdyenNotificationItemModel::class.java).amount?.value,
                    currency = objectMapper.readValue(it.message, AdyenNotificationItemModel::class.java).amount?.currency,
                    reason = objectMapper.readValue(it.message, AdyenNotificationItemModel::class.java).reason
                ),
                transactionAction = adyenActionToTransactionAction(it.notificationId.pspEvent, false),
                transactionStatus = if (objectMapper.readValue(
                        it.message,
                        AdyenNotificationItemModel::class.java
                    ).success == "true"
                ) TransactionStatus.SUCCESS.name else TransactionStatus.FAIL.name
            )
        }.collect(Collectors.toList())

        val response = khttp.put(
            url = paymentURL,
            headers = mapOf("API-KEY" to paymentApiKey),
            json = JSONObject(objectMapper.writeValueAsString(PspNotificationListRequestModel().apply { this.notifications.addAll(notificationModels) }))
        )

        notifications.forEach {
            it.status =
                if (response.statusCode == HttpStatus.CREATED.value()) NotificationStatus.SUCCESS else NotificationStatus.FAIL
            notificationRepository.save(it)
        }
    }

    private fun adyenActionToTransactionAction(adyenStatus: String?, preAuth: Boolean): String? {
        return when (adyenStatus) {
            "AUTHORISATION" -> {
                if (preAuth) TransactionAction.PREAUTH.name else TransactionAction.AUTH.name
            }
            "CAPTURE" -> TransactionAction.CAPTURE.name
            "REFUND" -> TransactionAction.REFUND.name
            "CANCELLATION" -> TransactionAction.REVERSAL.name
            else -> null
        }
    }
}
