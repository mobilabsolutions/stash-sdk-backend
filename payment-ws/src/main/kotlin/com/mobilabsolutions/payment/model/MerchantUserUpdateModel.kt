package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Merchant user update model")
data class MerchantUserUpdateModel(
    @ApiModelProperty(value = "First name of the merchant user", example = "Max")
    val firstname: String?,

    @ApiModelProperty(value = "Last name of the merchant user", example = "Mustermann")
    val lastname: String?,

    @ApiModelProperty(value = "Locale of the merchant user", example = "de-DE")
    val locale: String?
)
