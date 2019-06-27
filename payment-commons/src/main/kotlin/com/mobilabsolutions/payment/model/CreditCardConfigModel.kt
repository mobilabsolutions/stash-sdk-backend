package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.validation.CCMaskValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Credit Card Configuration")
data class CreditCardConfigModel(
    @ApiModelProperty(value = "Credit card mask", example = "1111")
    @field:CCMaskValidator
    val ccMask: String?,

    @ApiModelProperty(value = "Credit card expiry", example = "11/20")
    val ccExpiry: String?,

    @ApiModelProperty(value = "Credit card type", example = "VISA")
    val ccType: String?,

    @ApiModelProperty(value = "Credit card holder name", example = "Max Mustermann")
    val ccHolderName: String?
)
