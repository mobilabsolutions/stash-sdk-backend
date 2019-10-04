/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.validation.CountryCodeValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Personal data model")
data class PersonalDataModel(
    @ApiModelProperty(value = "Email address", example = "test@test.com")
    @field:Email
    val email: String?,

    @ApiModelProperty(value = "Customer's IP", example = "61.294.12.12")
    val customerIP: String?,

    @ApiModelProperty(value = "First name", example = "Max")
    val firstName: String?,

    @ApiModelProperty(value = "Last name", example = "Mustermann")
    val lastName: String?,

    @ApiModelProperty(value = "Street name", example = "Holzmarkt 59-65")
    val street: String?,

    @ApiModelProperty(value = "ZIP code", example = "50676")
    val zip: String?,

    @ApiModelProperty(value = "City", example = "Cologne")
    val city: String?,

    @ApiModelProperty(value = "Country", example = "DE")
    @field:CountryCodeValidator
    val country: String?,

    @ApiModelProperty(value = "Customer reference", example = "oIXHpTAfEPSleWXT6Khe")
    val customerReference: String?
)
