package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Payment methods overview model")
data class PaymentMethodsOverviewModel(
    @ApiModelProperty(value = "Week day", example = "Monday")
    val day: String?,

    @ApiModelProperty(value = "Payment methods data model")
    val paymentMethodData: MutableList<PaymentMethodDataModel> = mutableListOf()
)
