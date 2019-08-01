package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.NotificationModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Notification response model")
data class NotificationsResponseModel(
    @ApiModelProperty(value = "The notifications")
    val notifications: List<NotificationModel?> = mutableListOf(),

    @ApiModelProperty(value = "The number of transactions from yesterday", example = "20")
    val transactions: Int?
)
