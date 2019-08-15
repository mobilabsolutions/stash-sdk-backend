package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.model.MerchantNotificationsModel
import mu.KLogging
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class NotificationService(
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    fun sendNotificationToMerchant(webhookUrl: String, merchantNotifications: MutableList<MerchantNotificationsModel>): Int {
        logger.info("Forwarding notifications to the following url: $webhookUrl")
        val notificationsObject = JSONObject()
        notificationsObject.put("notifications", JSONArray(objectMapper.writeValueAsString(merchantNotifications)))
        return khttp.put(
            url = webhookUrl,
            json = notificationsObject
        ).statusCode
    }
}
