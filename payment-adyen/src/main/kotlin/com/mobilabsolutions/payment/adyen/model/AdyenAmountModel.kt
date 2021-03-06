/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Adyen amount model")
data class AdyenAmountModel(
    @ApiModelProperty("Amount in smallest currency unit (e.g. cent)", example = "300")
    @field:NotNull
    val value: Int?,

    @ApiModelProperty("Currency", example = "EUR")
    @field:Size(min = 3, max = 3)
    @field:NotNull
    val currency: String?
)
