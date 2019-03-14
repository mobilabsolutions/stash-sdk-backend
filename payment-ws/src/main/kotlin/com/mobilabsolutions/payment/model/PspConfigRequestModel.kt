package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Config Request")
data class PspConfigRequestModel(
    @ApiModelProperty(value = "Payment service provider type", example = "BSPAYONE")
    val pspId: String,

    @ApiModelProperty(value = "PSP configuration model")
    val pspConfig: PspUpsertConfigRequestModel
)