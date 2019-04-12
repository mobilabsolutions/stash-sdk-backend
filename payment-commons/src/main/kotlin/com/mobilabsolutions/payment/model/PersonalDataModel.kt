package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.validation.CountryCode
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Personal Data")
data class PersonalDataModel(
    @ApiModelProperty(value = "Email address", example = "test@test.com")
    @field:Email
    val email: String?,

    @ApiModelProperty(value = "First name of user", example = "Max")
    val firstName: String?,

    @ApiModelProperty(value = "Last name of user", example = "Mustermann")
    val lastName: String?,

    @ApiModelProperty(value = "Address information of account holder", example = "Holzmarkt 59-65")
    val street: String?,

    @ApiModelProperty(value = "ZIP code information of account holder", example = "50676")
    val zip: String?,

    @ApiModelProperty(value = "City of account holder", example = "Cologne")
    val city: String?,

    @ApiModelProperty(value = "Country code of account holder", example = "DE")
    @field:CountryCode
    val country: String?
)
