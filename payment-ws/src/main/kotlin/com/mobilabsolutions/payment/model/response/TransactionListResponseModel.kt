package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.TransactionListMetadata
import com.mobilabsolutions.payment.model.TransactionModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigInteger

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "List of Transactions")
data class TransactionListResponseModel(
    @ApiModelProperty(value = "Transaction list metadata")
    val metadata: TransactionListMetadata?,

    @ApiModelProperty(value = "Transaction Model")
    val transactions: MutableList<TransactionModel> = mutableListOf()
) {
    constructor(transactions: List<Array<Any>>, offset: Int?, limit: Int?) : this(
        TransactionListMetadata(if (!transactions.isEmpty()) transactions[0][9] as BigInteger else null, transactions.size, offset, limit),
        transactions.asSequence().map { TransactionModel(it) }.toMutableList()
    )
}
