/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Merchant user request model")
data class MerchantUserRequestModel(
    @ApiModelProperty(value = "Email", example = "test@mblb.net")
    @field:NotNull
    val email: String,

    @ApiModelProperty(value = "Password", example = "m5jSzBRQvaBS6FQ4")
    @field:NotNull
    val password: String,

    @ApiModelProperty(value = "First name", example = "Max")
    val firstname: String?,

    @ApiModelProperty(value = "Last name", example = "Mustermann")
    val lastname: String?,

    @ApiModelProperty(value = "Locale", example = "de-DE")
    val locale: String?
)
