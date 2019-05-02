package com.mobilabsolutions.payment.adyen.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Adyen Verify Payment Response")
data class AdyenVerifyPaymentResponseModel(
    @ApiModelProperty(value = "Status", example = "422")
    val status: Int?,

    @ApiModelProperty(value = "Error code", example = "14_017")
    val errorCode: String?,

    @ApiModelProperty(value = "Message", example = "The provided SDK Token has an invalid timestamp")
    val message: String?,

    @ApiModelProperty(value = "Error type", example = "Validation")
    val errorType: String?,

    @ApiModelProperty(value = "PSP reference", example = "sje324andls")
    val pspReference: String?
)
