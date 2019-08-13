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

    @ApiModelProperty(value = "Registration reference")
    val authenticationToken: String?,

    @ApiModelProperty(value = "Action type", example = "threeDS2Fingerprint")
    val type: String?,

    @ApiModelProperty(value = "Payment method type", example = "scheme")
    val paymentMethodType: String?
)
