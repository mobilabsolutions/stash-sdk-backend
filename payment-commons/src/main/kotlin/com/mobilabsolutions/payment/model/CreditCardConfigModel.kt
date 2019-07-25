/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Size

@ApiModel(value = "Credit card config model")
data class CreditCardConfigModel(
    @ApiModelProperty(value = "Credit card mask", example = "1111")
    @field:Size(min = 4, max = 4)
    val ccMask: String?,

    @ApiModelProperty(value = "Credit card expiry", example = "11/20")
    val ccExpiry: String?,

    @ApiModelProperty(value = "Credit card type", example = "VISA")
    val ccType: String?,

    @ApiModelProperty(value = "Credit card holder name", example = "Max Mustermann")
    val ccHolderName: String?,

    @ApiModelProperty(value = "Credit card nonce", example = "cnbaskjcbjakbjv")
    val nonce: String?,

    @ApiModelProperty(value = "Credit card device data", example = "{\"correlation_id\":\"73e463b6abef4de690f3b90c940b54d3\"}")
    val deviceData: String?
)
