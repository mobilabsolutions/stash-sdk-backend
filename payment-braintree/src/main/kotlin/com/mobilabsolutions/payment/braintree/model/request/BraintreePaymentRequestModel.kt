/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Braintree payment request model")
data class BraintreePaymentRequestModel(
    @ApiModelProperty(value = "Amount in decimal format", example = "5.00")
    @field:NotNull
    val amount: String?,

    @ApiModelProperty(value = "Payment token, psp alias in Payment SDK", example = "jdklaoa")
    val token: String?,

    @ApiModelProperty(value = "Device aata", example = "device 1")
    val deviceData: String?
)
