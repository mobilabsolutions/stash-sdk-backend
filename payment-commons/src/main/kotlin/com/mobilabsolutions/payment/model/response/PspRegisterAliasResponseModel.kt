/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP register alias response model")
data class PspRegisterAliasResponseModel(
    @ApiModelProperty(value = "Payment service provider alias", example = "jdklaoa")
    val pspAlias: String?,

    @ApiModelProperty(value = "Billing agreement id")
    val billingAgreementId: String?,

    @ApiModelProperty(value = "Registration reference")
    val registrationReference: String?,

    @ApiModelProperty(value = "Payload needed to verify the payment", example = "Ab02b4c0!BQABAgCYHYurjVnu8GRyhy1ZsGj...")
    val paymentData: String?,

    @ApiModelProperty(value = "Result code", example = "IdentifyShopper")
    val resultCode: String?,

    @ApiModelProperty(value = "Fingerprint or challenge token")
    val token: String?,

    @ApiModelProperty(value = "Action type", example = "threeDS2Fingerprint")
    val actionType: String?,

    @ApiModelProperty(value = "Payment method type", example = "scheme")
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
