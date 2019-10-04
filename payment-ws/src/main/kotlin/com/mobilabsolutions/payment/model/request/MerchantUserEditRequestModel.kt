/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Merchant user edit request model")
data class MerchantUserEditRequestModel(
    @ApiModelProperty(value = "First name", example = "Max")
    val firstname: String?,

    @ApiModelProperty(value = "Last name", example = "Mustermann")
    val lastname: String?,

    @ApiModelProperty(value = "Locale", example = "de-DE")
    val locale: String?
)
