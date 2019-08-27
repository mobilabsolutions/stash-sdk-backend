package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Live transaction model")
data class LiveTransactionModel(
    @ApiModelProperty(value = "Transaction created date", example = "2019-08-27T10:21:07.097")
    val createdDate: String?,

    @ApiModelProperty(value = "Transaction id", example = "V1g2bT1UcpcJSVK6whfW")
    val transactionId: String?,

    @ApiModelProperty(value = "Transaction amount", example = "2250")
    val amount: Int?,

    @ApiModelProperty(value = "Transaction currency id", example = "EUR")
    val currencyId: String?,

    @ApiModelProperty(value = "Payment method", example = "CC, SEPA, PAY_PAL")
    val paymentMethod: String?,

    @ApiModelProperty(value = "Transaction status", example = "SUCCESS")
    val status: String?,

    @ApiModelProperty(value = "Transaction action", example = "CAPTURE")
    val action: String?,

    @ApiModelProperty(value = "Merchant id", example = "mobilab")
    val merchantId: String?,

    @ApiModelProperty(value = "Indicator whether the notification is received or not", example = "true")
    val notification: Boolean?
)
