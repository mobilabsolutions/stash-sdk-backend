/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Size

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Credit card config model")
data class CreditCardConfigModel(
    @ApiModelProperty(value = "Credit card mask", example = "1111")
    @field:Size(min = 4, max = 4)
    val ccMask: String?,

    @ApiModelProperty(value = "Credit card expiry", example = "11/20")
    val ccExpiry: String?,

    @ApiModelProperty(value = "Credit card type", example = "VISA")
    val ccType: String?,

    @ApiModelProperty(value = "Credit card holder name", example = "Max Mustermann")
    val ccHolderName: String?,

    @ApiModelProperty(value = "Encrypted card number", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedCardNumber: String?,

    @ApiModelProperty(value = "Encrypted expiry month", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedExpiryMonth: String?,

    @ApiModelProperty(value = "Encrypted expiry year", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedExpiryYear: String?,

    @ApiModelProperty(value = "Encrypted security code", example = "adyenjs_0_1_18MT6ppy0FAMVMLH...")
    val encryptedSecurityCode: String?,

    @ApiModelProperty(value = "Return URL", example = "payment-dev.mblb.net")
    val returnUrl: String?,

    @ApiModelProperty(value = "Credit card nonce", example = "cnbaskjcbjakbjv")
    val nonce: String?,

    @ApiModelProperty(value = "Credit card device data", example = "{\"correlation_id\":\"73e463b6abef4de690f3b90c940b54d3\"}")
    val deviceData: String?
)
