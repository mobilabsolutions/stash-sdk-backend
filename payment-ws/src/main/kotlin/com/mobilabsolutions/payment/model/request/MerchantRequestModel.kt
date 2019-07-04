/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.validation.TimeZoneValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Merchant Creation Request")
data class MerchantRequestModel(
    @ApiModelProperty(value = "Merchant id", example = "Mobilab")
    @field:NotNull
    @field:Pattern(regexp = "^\\S+\$")
    val id: String,

    @ApiModelProperty(value = "Merchant name", example = "Mobilab's Best Merchant")
    @field:NotNull
    val name: String,

    @ApiModelProperty(value = "Merchant email", example = "merchant@mobilabsolutions.com")
    @field:NotNull
    @field:Email
    val email: String,

    @ApiModelProperty(value = "Merchant default currency", example = "EUR")
    @field:NotNull
    val currency: String?,

    @ApiModelProperty(value = "Merchant time zone", example = "Europe/Berlin")
    @field:TimeZoneValidator
    val timezone: String?
)
