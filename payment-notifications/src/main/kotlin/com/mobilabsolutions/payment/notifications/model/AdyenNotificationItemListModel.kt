/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AdyenNotificationItemListModel(
    @JsonProperty(value = "NotificationRequestItem")
    @field:Valid
    val notificationRequestItem: AdyenNotificationItemModel?
)
