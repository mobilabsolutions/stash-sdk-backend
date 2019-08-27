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

    @ApiModelProperty(value = "Fingerprint or challenge token", example = "eyJ0aH...")
    val token: String?,

    @ApiModelProperty(value = "Payment data", example = "eyJ0aH...")
    val paymentData: String?,

    @ApiModelProperty(value = "Adyen action type", example = "threeDS2Fingerprint")
    val actionType: String?,

    @ApiModelProperty(value = "Adyen payment method type", example = "scheme")
    val paymentMethodType: String?,

    @ApiModelProperty(value = "Payload needed when redirecting the shopper", example = "eyJ0aH...")
    val paReq: String?,

    @ApiModelProperty(value = "The return url provided in the request", example = "https://payment-dev.mblb.net")
    val termUrl: String?,

    @ApiModelProperty(value = "Payload needed to complete the payment", example = "OEVudmZVMUlkWjd0MDNwUWs2bmhSdz09...")
    val md: String?,

    @ApiModelProperty(value = "Url where shopper will be redirected", example = "https://test.adyen.com/hpp/3d/validate.shtml")
    val url: String?
)
