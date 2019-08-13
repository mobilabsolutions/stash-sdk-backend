package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Exchange Alias Response")
data class Alias3DSResponseModel(
    @ApiModelProperty(value = "Result code", example = "IdentifyShopper")
    val resultCode: String?,

    @ApiModelProperty(value = "Fingerptint or challenge token", example = "eyJ0aH...")
    val token: String?,

    @ApiModelProperty(value = "Payment data", example = "eyJ0aH...")
    val paymentData: String?,

    @ApiModelProperty(value = "Adyen action type", example = "threeDS2Fingerprint")
    val actionType: String?,

    @ApiModelProperty(value = "Adyen payment method type", example = "scheme")
    val paymentMethodType: String?
)
