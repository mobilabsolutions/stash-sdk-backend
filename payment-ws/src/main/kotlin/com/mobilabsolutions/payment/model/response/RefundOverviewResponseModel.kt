package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.RefundOverviewModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.LinkedHashMap

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Refund overview response model")
data class RefundOverviewResponseModel(
    @ApiModelProperty(value = "Refund overview model")
    val refunds: MutableList<RefundOverviewModel> = mutableListOf()
) {
    constructor(refunds: LinkedHashMap<String, Int>) : this(
        refunds.asSequence().map { RefundOverviewModel(it.key, it.value) }.toMutableList()
    )
}
