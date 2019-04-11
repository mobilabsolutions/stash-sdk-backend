package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "BS Payone Refund Request")
data class BsPayoneRefundRequestModel(
    @ApiModelProperty(value = "BS Payone transaction id", example = "42949")
    @JsonProperty(value = "txid")
    @field:NotNull
    val pspTransactionId: String?,

    @ApiModelProperty(value = "BS Payone Sequence Number", example = "CC: authorization 0, refund 1. SEPA: preauthorization 0, capture 1, refund 2")
    @JsonProperty(value = "sequencenumber")
    @field:NotNull
    val sequenceNumber: Int?,

    @ApiModelProperty(value = "BS Payone amount in smallest currency unit (e.g. cent)", example = "500")
    @field:NotNull
    val amount: String?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    @field:NotNull
    val currency: String?
)
