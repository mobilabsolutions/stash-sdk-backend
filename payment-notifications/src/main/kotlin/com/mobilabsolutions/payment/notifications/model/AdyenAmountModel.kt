package com.mobilabsolutions.payment.notifications.model

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AdyenAmountModel(
    @ApiModelProperty("Amount in smallest currency unit (e.g. cent)", example = "300")
    @field:NotNull
    val amount: Int?,

    @ApiModelProperty("Currency", example = "EUR")
    @field:Size(min = 3, max = 3)
    @field:NotNull
    val currency: String?
)
