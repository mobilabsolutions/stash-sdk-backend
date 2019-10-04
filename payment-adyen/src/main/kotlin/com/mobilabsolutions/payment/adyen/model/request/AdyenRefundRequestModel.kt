/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen refund request model")
data class AdyenRefundRequestModel(
    @ApiModelProperty(value = "PSP transaction id - Adyen PSP reference", example = "kdcnvbfkhbvka")
    val originalReference: String?,

    @ApiModelProperty(value = "Amount to be refunded")
    val modificationAmount: AdyenAmountRequestModel?,

    @ApiModelProperty(value = "Reference", example = "yourShopperId_IOfW3k9G2PvXFu2j")
    val reference: String?,

    @ApiModelProperty(value = "Merchant Account", example = "mobilab")
    val merchantAccount: String?
)
