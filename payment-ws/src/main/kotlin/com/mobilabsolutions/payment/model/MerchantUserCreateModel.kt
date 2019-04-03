package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Merchant user create model")
data class MerchantUserCreateModel(
    @ApiModelProperty(value = "Email of the merchant user", example = "test@mblb.net")
    @field:NotNull
    val email: String,

    @ApiModelProperty(value = "Password of the merchant user", example = "m5jSzBRQvaBS6FQ4")
    @field:NotNull
    val password: String,

    @ApiModelProperty(value = "First name of the merchant user", example = "Max")
    val firstname: String?,

    @ApiModelProperty(value = "Last name of the merchant user", example = "Mustermann")
    val lastname: String?,

    @ApiModelProperty(value = "Locale of the merchant user", example = "de-DE")
    val locale: String?
)
