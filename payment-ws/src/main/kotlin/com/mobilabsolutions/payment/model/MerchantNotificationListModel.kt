/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel("Merchant notification list model")
data class MerchantNotificationListModel(
    @ApiModelProperty(value = "List of merchant notifications")
    val notifications: MutableList<MerchantNotificationsModel> = mutableListOf()
)
