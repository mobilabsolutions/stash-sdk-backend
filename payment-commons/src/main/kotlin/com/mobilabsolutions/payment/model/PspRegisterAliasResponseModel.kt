package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Register Alias Response")
data class PspRegisterAliasResponseModel(
    @ApiModelProperty(value = "Payment service provider alias", example = "jdklaoa")
    val pspAlias: String?,

    @ApiModelProperty(value = "Braintree's billing agreement id")
    val billingAgreementId: String?
)
