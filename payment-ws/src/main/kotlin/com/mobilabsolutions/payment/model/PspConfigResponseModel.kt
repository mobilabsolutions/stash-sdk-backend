package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Config Response")
data class PspConfigResponseModel(
    @ApiModelProperty(value = "Payment service provider type", example = "BS_PAYONE")
    val pspId: String
)