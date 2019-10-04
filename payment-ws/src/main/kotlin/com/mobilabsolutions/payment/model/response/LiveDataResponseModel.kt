/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.KeyPerformanceModel
import com.mobilabsolutions.payment.model.NotificationsModel
import com.mobilabsolutions.payment.model.TodaysActivityModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Live data response model")
data class LiveDataResponseModel(
    @ApiModelProperty(value = "Key performance")
    val keyPerformance: KeyPerformanceModel?,

    @ApiModelProperty(value = "Today's activity")
    val todaysActivity: TodaysActivityModel?,

    @ApiModelProperty(value = "Notifications model")
    val notifications: NotificationsModel?
)
