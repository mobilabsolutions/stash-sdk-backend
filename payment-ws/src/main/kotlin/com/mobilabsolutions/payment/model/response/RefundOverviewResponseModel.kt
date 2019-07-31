package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.RefundOverviewModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Refund Overview Response Model")
data class RefundOverviewResponseModel(
    @ApiModelProperty(value = "Refund Overview Model")
    val refunds: MutableList<RefundOverviewModel> = mutableListOf()
) {
    constructor(refunds: HashMap<String, Int>) : this(
        refunds.asSequence().map { RefundOverviewModel(it.key, it.value) }.toMutableList()
    )
}
