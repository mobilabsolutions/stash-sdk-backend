package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Adyen Payment Request")
data class AdyenPaymentRequestModel(
    @ApiModelProperty(value = "Amount")
    val amount: AdyenAmountRequestModel,

    @ApiModelProperty(value = "Shopper email", example = "shopper@test.com")
    val shopperEmail: String?,

    @ApiModelProperty(value = "Shopper IP", example = "61.294.12.12")
    val shopperIP: String?,

    @ApiModelProperty(value = "Shopper reference", example = "yourShopperId_IOfW3k9G2PvXFu2j")
    val shopperReference: String?,

    @ApiModelProperty(value = "Selected recurring detail reference", example = "LATEST")
    val selectedRecurringDetailReference: String?,

    @ApiModelProperty(value = "Recurring")
    val recurring: AdyenRecurringRequestModel?,

    @ApiModelProperty(value = "Shopper interaction", example = "ContAuth")
    val shopperInteraction: String?,

    @ApiModelProperty(value = "Reference to identify a payment", example = "12345")
    val reference: String?,

    @ApiModelProperty(value = "Merchant account identifier", example = "MobiLab")
    val merchantAccount: String?,

    @ApiModelProperty(value = "Time between authorization and auto-capture, will be used for authorization", example = "0")
    val captureDelayHours: Int?
)
