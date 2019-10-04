/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.TransactionListMetadataModel
import com.mobilabsolutions.payment.model.TransactionModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigInteger

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Transaction list response model")
data class TransactionListResponseModel(
    @ApiModelProperty(value = "Transaction list metadata")
    val metadata: TransactionListMetadataModel?,

    @ApiModelProperty(value = "Transaction model")
    val transactions: MutableList<TransactionModel> = mutableListOf()
) {
    constructor(transactions: List<Array<Any>>, offset: Int?, limit: Int?, timezone: String?) : this(
        TransactionListMetadataModel(if (!transactions.isEmpty()) transactions[0][9] as BigInteger else null, transactions.size, offset, limit),
        transactions.asSequence().map { TransactionModel(it, timezone) }.toMutableList()
    )
}
