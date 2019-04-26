package com.mobilabsolutions.payment.adyen.model.response

import io.swagger.annotations.ApiModel

@ApiModel(value = "Adyen Verify Payment Response")
data class AdyenVerifyPaymentResponseModel(
    val status: Int?,

    val errorCode: String?,

    val message: String?,

    val errorType: String?
)
