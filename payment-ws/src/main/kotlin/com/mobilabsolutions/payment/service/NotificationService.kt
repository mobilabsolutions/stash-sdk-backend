/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.model.MerchantNotificationListModel
import com.mobilabsolutions.payment.model.MerchantNotificationsModel
import mu.KLogging
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class NotificationService {
    companion object : KLogging()

    /**
     * Sends notification to the merchant
     *
     * @param webhookUrl url to send the notification to
     * @param merchantNotifications the list of merchant notifications
     */
    fun sendNotificationToMerchant(webhookUrl: String, merchantNotifications: MutableList<MerchantNotificationsModel>): Int {
        return khttp.put(
            url = webhookUrl,
            json = JSONObject(MerchantNotificationListModel().apply {
                notifications.addAll(merchantNotifications)
            }
        )).statusCode
    }
}
