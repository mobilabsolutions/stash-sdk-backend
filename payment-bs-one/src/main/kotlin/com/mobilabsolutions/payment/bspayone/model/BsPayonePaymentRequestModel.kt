package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Payment Request")
data class BsPayonePaymentRequestModel(
    @ApiModelProperty(value = "BS Payone account id", example = "42949")
    @JsonProperty(value = "aid")
    @field:NotNull
    val accountId: String?,

    @ApiModelProperty(value = "BS Payone clearing type", example = "cc")
    @JsonProperty(value = "clearingtype")
    @field:NotNull
    val clearingType: String?,

    @ApiModelProperty(value = "BS Payone reference", example = "8DANasUsXZ")
    @field:NotNull
    val reference: String?,

    @ApiModelProperty(value = "BS Payone amount in smallest currency unit (e.g. cent)", example = "500")
    @field:NotNull
    val amount: String?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    @field:NotNull
    val currency: String?,

    @ApiModelProperty(value = "Customer id", example = "12378")
    @JsonProperty(value = "customerid")
    @field:NotNull
    val customerId: String?,

    @ApiModelProperty(value = "Last name", example = "Mustermann")
    @JsonProperty(value = "lastname")
    @field:NotNull
    val lastName: String?,

    @ApiModelProperty(value = "Country", example = "Germany")
    @field:NotNull
    val country: String?,

    @ApiModelProperty(value = "City", example = "Cologne")
    @field:NotNull
    val city: String?,

    @ApiModelProperty(value = "BS Payone credit card alias")
    @JsonProperty(value = "pseudocardpan")
    val pspAlias: String?,

    @ApiModelProperty(value = "International Bank Account Number", example = "DE00123456782599100004")
    val iban: String?,

    @ApiModelProperty(value = "Bank Identifier Code", example = "TESTTEST")
    val bic: String?
)