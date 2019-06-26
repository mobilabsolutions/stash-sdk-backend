package com.mobilabsolutions.payment.notifications.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AdyenNotificationItemListModel(
    @JsonProperty(value = "NotificationRequestItem")
    val notificationRequestItem: AdyenNotificationItemModel?
)
