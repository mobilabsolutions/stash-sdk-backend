package com.mobilabsolutions.payment.message

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "SEPA Configuration")
data class SepaConfigModel(
    @ApiModelProperty(value = "International Bank Account Number", example = "DE00123456782599100004")
    val iban: String?,

    @ApiModelProperty(value = "Bank Identifier Code", example = "TESTTEST")
    val bic: String?,

    @ApiModelProperty(value = "Firstname of account holder", example = "Max")
    val firstname: String?,

    @ApiModelProperty(value = "Lastname of account holder", example = "Mustermann")
    val lastname: String?,

    @ApiModelProperty(value = "Address information of account holder", example = "")
    val street: String?,

    @ApiModelProperty(value = "ZIP code information of account holder", example = "50676")
    val zip: String?,

    @ApiModelProperty(value = "Country code of account holder", example = "DE")
    val country: String?
)