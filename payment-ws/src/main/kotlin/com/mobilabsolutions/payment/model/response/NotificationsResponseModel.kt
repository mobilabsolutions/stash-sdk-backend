/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.DailyTransactionsModel
import com.mobilabsolutions.payment.model.NotificationModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.LinkedHashMap

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Notification response model")
data class NotificationsResponseModel(
    @ApiModelProperty(value = "The notifications")
    val notifications: List<NotificationModel?> = mutableListOf(),

    @ApiModelProperty(value = "All transactions for last week")
    val transactions: List<DailyTransactionsModel?> = mutableListOf()
) {
    constructor(notifications: List<NotificationModel?>, transactions: LinkedHashMap<String, Int>) : this(
        notifications,
        transactions.asSequence().map { DailyTransactionsModel(it.key, it.value) }.toMutableList()
    )
}
