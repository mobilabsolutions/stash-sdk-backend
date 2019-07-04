/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.model.request

import com.mobilabsolutions.payment.notifications.model.AdyenNotificationItemListModel
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AdyenNotificationRequestModel(
    val live: String?,
    @field:Valid
    val notificationItems: MutableList<AdyenNotificationItemListModel>?
)
