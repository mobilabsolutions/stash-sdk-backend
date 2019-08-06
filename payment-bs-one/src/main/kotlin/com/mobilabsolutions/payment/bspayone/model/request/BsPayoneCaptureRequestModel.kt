/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.bspayone.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "BS Payone Capture Request")
data class BsPayoneCaptureRequestModel(
    @ApiModelProperty(value = "BS Payone transaction id", example = "42949")
    @JsonProperty(value = "txid")
    @field:NotNull
    val pspTransactionId: String?,

    @ApiModelProperty(value = "BS Payone amount in smallest currency unit (e.g. cent)", example = "500")
    @field:NotNull
    val amount: String?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    @field:NotNull
    val currency: String?
)
