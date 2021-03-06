/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP register alias request model")
data class PspRegisterAliasRequestModel(
    @ApiModelProperty("Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    val aliasId: String?,

    @ApiModelProperty(value = "Alias extra model")
    val aliasExtra: AliasExtraModel?,

    @ApiModelProperty(value = "PSP config model")
    val pspConfig: PspConfigModel?
)
