/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.service.TransactionDetailsService
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.sql.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Timeline Model")
data class TransactionTimelineModel(
    @ApiModelProperty(value = "Amount in smallest currency unit (e.g. cent)", example = "2000")
    val amount: Int?,

    @ApiModelProperty(value = "Reason", example = "Payment for dinner")
    val reason: String?,

    @ApiModelProperty(value = "Transaction action")
    val action: String?,

    @ApiModelProperty(value = "Transaction status", example = "AUTHORIZED")
    val status: String?,

    @ApiModelProperty(value = "Creation Date", example = "2018-12-31 23:59:59.999 +0100")
    val createdDate: String?
) {
    constructor(transaction: Array<Any>, timezone: String?) : this(
        transaction[0] as Int?,
        transaction[1] as String?,
        transaction[2] as String?,
        transaction[3] as String?,
        DateTimeFormatter.ofPattern(TransactionDetailsService.DATE_FORMAT).withZone(ZoneId.of(timezone)).format((transaction[4] as Timestamp).toInstant())
    )
}
