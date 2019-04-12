package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email

@ApiModel(value = "Merchant Response Model")
data class MerchantResponseModel(
    @ApiModelProperty(value = "Merchant name", example = "Supermarket")
    val name: String?,

    @ApiModelProperty(value = "Merchant email", example = "test@mblb.net")
    @field:Email val
    email: String?,
    val defaultCurrency: String?
)
