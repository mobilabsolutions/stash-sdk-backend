/*
 * Copyright © MobiLab Solutions GmbH
 */

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
    val payload: String?
)
