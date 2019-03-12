package com.mobilabsolutions.payment.message

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email

@ApiModel(value = "SEPA Configuration")
data class SepaConfigModel(
    @ApiModelProperty(value = "International Bank Account Number", example = "DE00123456782599100004")
    val iban: String?,

    @ApiModelProperty(value = "Bank Identifier Code", example = "TESTTEST")
    val bic: String?,

    @ApiModelProperty(value = "Email address", example = "test@test.com")
    @field:Email
    val email: String?,

    @ApiModelProperty(value = "Full name of account holder", example = "Max Mustermann")
    val name: String?,

    @ApiModelProperty(value = "Address information of account holder", example = "Holzmarkt 59-65")
    val street: String?,

    @ApiModelProperty(value = "ZIP code information of account holder", example = "50676")
    val zip: String?,

    @ApiModelProperty(value = "Country code of account holder", example = "DE")
    val country: String?
)