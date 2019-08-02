package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Key performance model")
data class KeyPerformanceModel(
    @ApiModelProperty(value = "The total amount earned in the last month", example = "4560")
    val salesVolume: Int?,

    @ApiModelProperty(value = "The number of nrOfTransactions in the last month", example = "200")
    val nrOfTransactions: Int?,

    @ApiModelProperty(value = "The number of refunded nrOfTransactions in the last month", example = "15")
    val nrOfRefundedTransactions: Int?,

    @ApiModelProperty(value = "The number of nrOfChargebacks in the last month", example = "10")
    val nrOfChargebacks: Int?
)
