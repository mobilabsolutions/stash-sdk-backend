package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Adyen Amount Request")
data class AdyenAmountRequestModel(
    @ApiModelProperty(value = "Adyen amount value in smallest currency unit (e.g. cent)", example = "2000")
    val value: Int?,

    @ApiModelProperty(value = "Adyen currency", example = "EUR")
    val currency: String?
)
