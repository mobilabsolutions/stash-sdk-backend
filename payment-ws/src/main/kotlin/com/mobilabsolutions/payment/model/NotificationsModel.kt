/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Notifications model")
data class NotificationsModel(
    @ApiModelProperty(value = "Notifications model")
    val notification: NotificationModel?,

    @ApiModelProperty(value = "The number of transactions from yesterday", example = "20")
    val nrOfTransactions: Int?
)
