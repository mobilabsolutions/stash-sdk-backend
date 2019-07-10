/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.model.PspNotificationModel
import com.mobilabsolutions.payment.model.request.PspNotificationListRequestModel
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class NotificationClient(
    private val objectMapper: ObjectMapper
) {
    /**
     * Sends notifications to sdk backend
     *
     * @param paymentURL Payment SDK backend url
     * @param paymentApiKey Api Key for auth
     * @param notifications Notification models
     * @return Http status code
     */
    fun sendNotifications(
        paymentURL: String,
        paymentApiKey: String,
        notifications: MutableList<PspNotificationModel>
    ): Int {
        return khttp.put(
            url = paymentURL,
            headers = mapOf("API-KEY" to paymentApiKey),
            json = JSONObject(objectMapper.writeValueAsString(PspNotificationListRequestModel().apply {
                this.notifications.addAll(
                    notifications
                )
            }))
        ).statusCode
    }
}
