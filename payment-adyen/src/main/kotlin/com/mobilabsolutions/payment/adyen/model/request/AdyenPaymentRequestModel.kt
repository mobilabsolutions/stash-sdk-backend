package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen Payment Request")
data class AdyenPaymentRequestModel(
    @ApiModelProperty(value = "Adyen amount")
    val amount: AdyenAmountRequestModel,

    @ApiModelProperty(value = "Shopper email", example = "shopper@test.com")
    val shopperEmail: String?,

    @ApiModelProperty(value = "Shopper IP", example = "61.294.12.12")
    val shopperIP: String?,

    @ApiModelProperty(value = "Shopper reference", example = "yourShopperId_IOfW3k9G2PvXFu2j")
    val shopperReference: String?,

    @ApiModelProperty(value = "Selected recurring detail reference", example = "LATEST")
    val selectedRecurringDetailReference: String?,

    @ApiModelProperty(value = "Adyen recurring")
    val recurring: AdyenRecurringRequestModel?,

    @ApiModelProperty(value = "Shopper interaction", example = "ContAuth")
    val shopperInteraction: String?,

    @ApiModelProperty(value = "Reference", example = "yourShopperId_IOfW3k9G2PvXFu2j")
    val reference: String?,

    @ApiModelProperty(value = "Merchant Account", example = "mobilab")
    val merchantAccount: String?,

    @ApiModelProperty(value = "Time between authorization and auto-capture, will be used for authorization", example = "0")
    val captureDelayHours: Int?,

    @ApiModelProperty(value = "Adyen payment method")
    val paymentMethod: AdyenPaymentMethodRequestModel?
)
