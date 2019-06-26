package com.mobilabsolutions.payment.notifications.model.request

import com.mobilabsolutions.payment.notifications.model.AdyenNotificationItemListModel

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AdyenNotificationRequestModel(
    val live: String?,
    val notificationItems: MutableList<AdyenNotificationItemListModel>?
)
