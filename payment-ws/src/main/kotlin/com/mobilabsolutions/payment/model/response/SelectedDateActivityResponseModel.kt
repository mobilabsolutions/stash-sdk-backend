package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.SelectedDateActivityModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.LinkedHashMap

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Selected date activity response model")
data class SelectedDateActivityResponseModel(
    @ApiModelProperty(value = "Selected date activity model")
    val transactions: MutableList<SelectedDateActivityModel> = mutableListOf()
) {
    constructor(transactions: LinkedHashMap<String, Int>) : this(
        transactions.asSequence().map { SelectedDateActivityModel(it.key, it.value) }.toMutableList()
    )
}
