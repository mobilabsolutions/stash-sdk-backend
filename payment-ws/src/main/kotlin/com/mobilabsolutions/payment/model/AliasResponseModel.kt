package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.message.PspAliasConfigModel
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