/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.PspAliasConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Alias Response")
data class AliasResponseModel(
    @ApiModelProperty(value = "Generated alias", example = "XXmmg2Sy9L6JrDvvgpjn")
    val aliasId: String?,

    @ApiModelProperty(value = "PSP Configuration")
    val psp: PspAliasConfigModel?
)
