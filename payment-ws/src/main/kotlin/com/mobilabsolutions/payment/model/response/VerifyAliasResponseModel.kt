package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Verify alias response model")
data class VerifyAliasResponseModel(
    @ApiModelProperty(value = "Result code", example = "ChallengeShopper")
    val resultCode: String?,

    @ApiModelProperty(value = "Token for performing 3DS 2 challenge", example = "eyJ0aH...")
    val challengeToken: String?,

    @ApiModelProperty(value = "Payment data", example = "eyJ0aH...")
    val paymentData: String?,

    @ApiModelProperty(value = "Adyen action type", example = "threeDS2Fingerprint")
    val actionType: String?,

    @ApiModelProperty(value = "Adyen payment method type", example = "scheme")
    val paymentMethodType: String?
)
