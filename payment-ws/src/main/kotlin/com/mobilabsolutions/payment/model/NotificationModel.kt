package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Notification model")
data class NotificationModel(
    @ApiModelProperty(value = "Payment method", example = "Credit Card")
    val paymentMethod: String?,

    @ApiModelProperty(value = "Notification content", example = "Refunded 100€")
    val content: String?,

    @ApiModelProperty(value = "Date", example = "2019-09-08 11:40:40")
    val date: String?
)
