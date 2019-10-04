/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Refund overview model")
data class RefundOverviewModel(
    @ApiModelProperty(value = "Week day", example = "Monday")
    val day: String?,

    @ApiModelProperty(value = "Total refunded amount", example = "5000")
    val amount: Int?
)
