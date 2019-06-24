package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.service.TransactionDetailsService
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.sql.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Transaction Response")
data class TransactionModel(
    @ApiModelProperty(value = "Transaction id", example = "frtdqw7m")
    val transactionId: String?,

    @ApiModelProperty(value = "Amount in smallest currency unit (e.g. cent)", example = "2000")
    val amount: Int?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currencyId: String?,

    @ApiModelProperty(value = "Transaction status", example = "SUCCESS")
    val status: String?,

    @ApiModelProperty(value = "Transaction action", example = "AUTHORIZED")
    val action: String?,

    @ApiModelProperty(value = "Reason", example = "Payment for dinner")
    val reason: String?,

    @ApiModelProperty(value = "Merchant customer id", example = "frefatdfqw7am")
    val customerId: String?,

    @ApiModelProperty(value = "Payment method", example = "Values: CC, SEPA, PAY_PAL, GOOGLE_PAY, APPLE_PAY, KLARNA")
    val paymentMethod: String?,

    @ApiModelProperty(value = "Creation Date", example = "2018-12-31 23:59:59.999 +0100")
    val createdDate: String?
) {
    constructor(transaction: Array<Any>, timezone: String?) : this(
        transaction[0] as String?,
        transaction[1] as Int?,
        transaction[2] as String?,
        transaction[3] as String?,
        transaction[4] as String?,
        transaction[5] as String?,
        transaction[6] as String?,
        transaction[7] as String?,
        DateTimeFormatter.ofPattern(TransactionDetailsService.DATE_FORMAT).withZone(ZoneId.of(timezone)).format((transaction[8] as Timestamp).toInstant())
    )
}
