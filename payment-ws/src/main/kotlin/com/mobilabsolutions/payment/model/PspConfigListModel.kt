/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP config list model")
data class PspConfigListModel(
    @ApiModelProperty(value = "List of PSP configuration")
    @field:Valid
    val psp: MutableList<PspConfigModel> = mutableListOf()
)
