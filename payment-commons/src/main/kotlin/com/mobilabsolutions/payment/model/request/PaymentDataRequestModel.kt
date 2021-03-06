/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Payment data request model")
data class PaymentDataRequestModel(
    @ApiModelProperty("Amount in smallest currency unit (e.g. cent)", example = "300")
    @field:NotNull
    val amount: Int?,

    @ApiModelProperty("Currency", example = "EUR")
    @field:Size(min = 3, max = 3)
    @field:NotNull
    val currency: String?,

    @ApiModelProperty("Reason", example = "Payment for dinner")
    @field:NotNull
    val reason: String?
)
