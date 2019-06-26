package com.mobilabsolutions.payment.adyen.model.request

import com.fasterxml.jackson.annotation.JsonProperty
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
    val paymentMethod: AdyenPaymentMethodRequestModel?,

    @ApiModelProperty(value = "Execute 3D Secure", example = "true")
    @JsonProperty(value = "additionalData.executeThreeD")
    val execute3D: String?,

    @ApiModelProperty(value = "Return URL", example = "payment-dev.mblb.net")
    val returnUrl: String?,

    @ApiModelProperty(value = "Enable recurring payment", example = "true")
    val enableRecurring: Boolean?
)
