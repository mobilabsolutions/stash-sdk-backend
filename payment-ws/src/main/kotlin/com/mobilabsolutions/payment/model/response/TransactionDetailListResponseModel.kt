package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Transaction Details List")
data class TransactionDetailListResponseModel(
    @ApiModelProperty(value = "Transaction Model")
    val transactions: MutableList<TransactionDetailsResponseModel> = mutableListOf()
)
