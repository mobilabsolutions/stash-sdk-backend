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
@ApiModel(value = "Merchant user update password request model")
data class MerchantUserEditPasswordRequestModel(
    @ApiModelProperty(value = "Old password", example = "old-password")
    @field:NotNull
    val oldPassword: String,

    @ApiModelProperty(value = "New password", example = "new-password")
    @field:NotNull
    val newPassword: String
)
