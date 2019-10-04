/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen amount request model")
data class AdyenAmountRequestModel(
    @ApiModelProperty(value = "Amount in smallest currency unit (e.g. cent)", example = "200")
    val value: Int?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currency: String?
)
