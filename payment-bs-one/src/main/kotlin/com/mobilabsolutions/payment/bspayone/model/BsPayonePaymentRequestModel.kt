package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Payment Request")
data class BsPayonePaymentRequestModel(
    @ApiModelProperty(value = "BS Payone account id", example = "42949")
    @JsonProperty(value = "aid")
    val accountId: String,

    @ApiModelProperty(value = "BS Payone clearing type", example = "cc")
    @JsonProperty(value = "clearingtype")
    val clearingType: String,

    @ApiModelProperty(value = "BS Payone reference", example = "Office material order")
    val reference: String,

    @ApiModelProperty(value = "BS Payone amount in smallest currency unit (e.g. cent)", example = "500")
    val amount: String,

    @ApiModelProperty(value = "Personal Data")
    val personalData: BsPayonePersonalDataModel,

    @ApiModelProperty(value = "BS Payone credit card alias")
    val creditCardAlias: BsPayoneCreditCardAliasModel?,

    @ApiModelProperty(value = "BS Payone sepa details")
    val sepaDetails: BsPayoneSepaDetailsModel?
)