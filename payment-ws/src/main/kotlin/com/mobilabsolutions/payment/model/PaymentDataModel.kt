package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Payment Data Model")
data class PaymentDataModel(
    @ApiModelProperty("Amount")
    @field:NotNull
    val amount: Int,

    @ApiModelProperty("Currency")
    val currency: String?,

    @ApiModelProperty("Reason")
    val reason: String?
)