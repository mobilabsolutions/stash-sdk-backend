package com.mobilabsolutions.payment.adyen.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Adyen Verify Payment Response")
data class AdyenVerifyPaymentResponseModel(
    @ApiModelProperty(value = "PSP reference", example = "sje324andls")
    val pspReference: String?,

    @ApiModelProperty(value = "Adyen result code", example = "Authorised")
    val resultCode: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?
)
