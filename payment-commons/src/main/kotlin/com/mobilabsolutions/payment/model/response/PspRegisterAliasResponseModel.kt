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

    val paymentData: String?,

    val resultCode: String?,

    val authenticationToken: String?
)
