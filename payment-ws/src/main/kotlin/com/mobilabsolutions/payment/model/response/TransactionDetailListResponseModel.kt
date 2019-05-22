package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Transaction Details List")
data class TransactionDetailListResponseModel(
    @ApiModelProperty(value = "Transaction Model")
    val transactions: MutableList<TransactionDetailsResponseModel> = mutableListOf()
)
