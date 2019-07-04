/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Transaction Report")
class TransactionReportModel(
    @ApiModelProperty(value = "Line number", example = "1")
    val no: Int?,

    @ApiModelProperty(value = "Transaction id", example = "frtdqw7m")
    val id: String?,

    @ApiModelProperty(value = "Amount", example = "20")
    val amount: Double?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currency: String?,

    @ApiModelProperty(value = "Transaction status", example = "Captured")
    val status: String?,

    @ApiModelProperty(value = "Reason", example = "Payment for dinner")
    val reason: String?,

    @ApiModelProperty(value = "Merchant customer id", example = "frefatdfqw7am")
    val customerId: String?,

    @ApiModelProperty(value = "Payment method", example = "Values: CC, SEPA, PAY_PAL, GOOGLE_PAY, APPLE_PAY, KLARNA")
    val paymentMethod: String?,

    @ApiModelProperty(value = "Creation Date", example = "2018-12-31 23:59:59.999 +0100")
    val createdDate: String?
)
