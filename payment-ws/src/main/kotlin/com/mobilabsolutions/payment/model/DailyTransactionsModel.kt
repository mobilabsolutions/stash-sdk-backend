package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Daily transaction model")
data class DailyTransactionsModel(
    @ApiModelProperty(value = "Week day", example = "Monday")
    val day: String?,

    @ApiModelProperty(value = "The number of transactions", example = "20")
    val nrOfTransactions: Int?
)
