package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Adyen Verify Payment Request")
data class AdyenVerifyPaymentRequestModel(
    @ApiModelProperty(value = "Adyen API Key")
    val apiKey: String?,

    @ApiModelProperty(value = "Adyen Payload")
    val payload: String?,

    @ApiModelProperty(value = "Sandbox (test) server url", example = "https://checkout-test.adyen.com")
    val sandboxServerUrl: String?,

    @ApiModelProperty(value = "Server url", example = "https://[random]-[company-name]-checkout-live.adyenpayments.com")
    val serverUrl: String?
)
