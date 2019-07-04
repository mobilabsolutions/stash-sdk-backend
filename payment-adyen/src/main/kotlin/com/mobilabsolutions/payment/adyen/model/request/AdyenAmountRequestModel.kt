/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen Amount")
data class AdyenAmountRequestModel(
    @ApiModelProperty(value = "Adyen amount value in smallest currency unit (e.g. cent)", example = "200")
    val value: Int?,

    @ApiModelProperty(value = "Adyen currency", example = "EUR")
    val currency: String?
)
