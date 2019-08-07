package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Selected data activity model")
data class SelectedDateActivityModel(
    @ApiModelProperty(value = "Hourly time range", example = "12-13")
    val timeRange: String?,

    @ApiModelProperty(value = "Total captured amount", example = "5000")
    val amount: Int?
)
