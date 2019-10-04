/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Braintree payment method response model")
data class BraintreeRegisterAliasResponseModel(
    @ApiModelProperty(value = "Payment token, psp alias in Payment SDK", example = "jdklaoa")
    val token: String?,

    @ApiModelProperty(value = "Unique identifier of the vaulted payment flow agreement between the customer's PayPal account and your PayPal business account")
    val billingAgreementId: String?
)
