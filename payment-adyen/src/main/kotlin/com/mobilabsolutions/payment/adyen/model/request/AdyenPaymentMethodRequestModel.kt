/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen Payment Method")
data class AdyenPaymentMethodRequestModel(
    @ApiModelProperty(value = "Adyen payment method type", example = "sepadirectdebit")
    val type: String?,

    @ApiModelProperty(value = "Account holder name", example = "M. Mustermann")
    @JsonProperty(value = "sepa.ownerName")
    val holderName: String?,

    @ApiModelProperty(value = "IBAN", example = "DE87123456781234567890")
    @JsonProperty(value = "sepa.ibanNumber")
    val iban: String?,

    @ApiModelProperty(value = "Encrypted card number", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedCardNumber: String?,

    @ApiModelProperty(value = "Encrypted expiry month", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedExpiryMonth: String?,

    @ApiModelProperty(value = "Encrypted expiry year", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedExpiryYear: String?,

    @ApiModelProperty(value = "Encrypted security code", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedSecurityCode: String?
)
