/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.model.AliasExtraModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Alias request model")
data class AliasRequestModel(
    @ApiModelProperty(value = "Payment service provider alias", example = "GygOIj76ighI7T98yHg98ty78tH0h0hT6960lMLnsSD")
    val pspAlias: String?,

    @ApiModelProperty(value = "Alias extra model")
    @field:Valid
    val extra: AliasExtraModel?
)
