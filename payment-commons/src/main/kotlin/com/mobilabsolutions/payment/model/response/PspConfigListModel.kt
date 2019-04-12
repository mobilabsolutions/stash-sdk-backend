package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.request.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "List of PSP Config Model")
data class PspConfigListModel(
    @ApiModelProperty(value = "List of PSP Configuration")
    val psp: MutableList<PspConfigModel> = mutableListOf()
)
