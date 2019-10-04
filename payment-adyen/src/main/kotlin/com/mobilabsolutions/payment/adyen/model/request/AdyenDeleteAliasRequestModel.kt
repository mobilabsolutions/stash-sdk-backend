/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Adyen delete alias request model")
data class AdyenDeleteAliasRequestModel(
    @ApiModelProperty(value = "Shopper reference", example = "yourShopperId_IOfW3k9G2PvXFu2j")
    val shopperReference: String?,

    @ApiModelProperty(value = "Recurring detail reference", example = "8314442372419167")
    val recurringDetailReference: String?,

    @ApiModelProperty(value = "Merchant account", example = "mobilab")
    val merchantAccount: String?
)
