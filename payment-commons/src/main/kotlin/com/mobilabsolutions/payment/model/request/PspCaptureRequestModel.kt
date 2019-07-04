/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.model.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Capture Request")
data class PspCaptureRequestModel(
    @ApiModelProperty(value = "PSP transaction id", example = "42949")
    val pspTransactionId: String?,

    @ApiModelProperty(value = "Amount in smallest currency unit (e.g. cent)", example = "500")
    val amount: Int?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currency: String?,

    @ApiModelProperty(value = "PSP config")
    val pspConfig: PspConfigModel,

    @ApiModelProperty("Purchase ID", example = "132")
    val purchaseId: String?
)
