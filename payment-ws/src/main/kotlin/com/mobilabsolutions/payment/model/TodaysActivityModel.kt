/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Today's activity model")
data class TodaysActivityModel(
    @ApiModelProperty(value = "Time in hours", example = "12:00:00")
    val time: String?,

    @ApiModelProperty(value = "Captured amount", example = "1200")
    val amount: Int?
)
