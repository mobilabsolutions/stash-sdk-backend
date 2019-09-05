package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.FiltersModel
import io.swagger.annotations.ApiModel

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Filters response model")
data class FiltersListResponseModel(
    val filters: List<FiltersModel>
)