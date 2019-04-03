package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
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
    val email: String,

    @ApiModelProperty(value = "Merchant default currency", example = "EUR")
    val currency: String?
)
