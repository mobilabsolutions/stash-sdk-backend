package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.model.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Reversal Request")
data class PspReversalRequestModel(
    @ApiModelProperty(value = "PSP transaction id", example = "42949")
    val pspTransactionId: String?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currency: String?,

    @ApiModelProperty(value = "PSP config")
    val pspConfig: PspConfigModel,

    @ApiModelProperty(value = "Merchant Transaction ID", example = "12345")
    val merchantTransactionId: String?
)
