package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Alias Request")
data class AliasRequestModel(
    @ApiModelProperty(value = "Payment service provider alias", example = "GygOIj76ighI7T98yHg98ty78tH0h0hT6960lMLnsSD")
    val pspAlias: String?,

    @ApiModelProperty(value = "Alias Extra")
    @field:Valid
    val extra: AliasExtraModel?
)