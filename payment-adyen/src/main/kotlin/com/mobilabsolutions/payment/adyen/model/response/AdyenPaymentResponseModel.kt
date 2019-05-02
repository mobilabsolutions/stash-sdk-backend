package com.mobilabsolutions.payment.adyen.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Adyen Payment Response")
data class AdyenPaymentResponseModel(
    @ApiModelProperty(value = "Authorisation code", example = "123")
    val authCode: String?,

    @ApiModelProperty(value = "PSP reference", example = "sje324andls")
    val pspReference: String?,

    @ApiModelProperty(value = "Refusal reason", example = "Failed transaction")
    val refusalReason: String?,

    @ApiModelProperty(value = "Result code", example = "Authorised")
    val resultCode: String?
)
