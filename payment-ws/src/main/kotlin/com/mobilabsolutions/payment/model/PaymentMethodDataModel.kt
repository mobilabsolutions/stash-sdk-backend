package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Payment methods data model")
data class PaymentMethodDataModel(
    @ApiModelProperty(value = "Payment method", example = "Values: CC, SEPA, PAY_PAL, GOOGLE_PAY, APPLE_PAY, KLARNA")
    val paymentMethod: String?,

    @ApiModelProperty(value = "Total refunded amount", example = "5000")
    val amount: Int?
)
