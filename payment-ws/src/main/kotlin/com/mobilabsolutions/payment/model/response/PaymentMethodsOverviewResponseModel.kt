package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.PaymentMethodDataModel
import com.mobilabsolutions.payment.model.PaymentMethodsOverviewModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Payment methods overview response model")
data class PaymentMethodsOverviewResponseModel(
    @ApiModelProperty(value = "Payment methods overview model")
    val transactions: MutableList<PaymentMethodsOverviewModel> = mutableListOf()
) {
    constructor(transactions: LinkedHashMap<String, LinkedHashMap<String, Int>>) : this(
        transactions.asSequence().map { PaymentMethodsOverviewModel(it.key, it.value.map { PaymentMethodDataModel(it.key, it.value) }.toMutableList()) }.toMutableList()
    )
}
