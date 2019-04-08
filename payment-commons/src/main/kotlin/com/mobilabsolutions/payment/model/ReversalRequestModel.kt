package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Reversal Request Model")
data class ReversalRequestModel(
    @ApiModelProperty("Reason", example = "Payment for dinner")
    @field:NotNull
    val reason: String?
)
