package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Merchant user change password model")
data class MerchantUserChangePasswordModel(
    @ApiModelProperty(value = "Old password of the merchant user", example = "old-password")
    @field:NotNull
    val oldPassword: String,

    @ApiModelProperty(value = "New password of the merchant user", example = "new-password")
    @field:NotNull
    val newPassword: String
)
