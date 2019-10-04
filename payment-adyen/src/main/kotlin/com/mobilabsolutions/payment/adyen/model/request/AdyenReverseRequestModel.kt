/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Adyen reverse request models")
data class AdyenReverseRequestModel(
    @ApiModelProperty(value = "Adyen PSP reference", example = "42949")
    val originalReference: String?,

    @ApiModelProperty(value = "Reference", example = "yourShopperId_IOfW3k9G2PvXFu2j")
    val reference: String?,

    @ApiModelProperty(value = "Merchant Account", example = "mobilab")
    val merchantAccount: String?
)
