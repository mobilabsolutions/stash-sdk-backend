package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.TransactionModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "List of Transactions")
data class TransactionListResponseModel(
    @ApiModelProperty(value = "Transaction Model")
    val transactions: MutableList<TransactionModel> = mutableListOf()
)
