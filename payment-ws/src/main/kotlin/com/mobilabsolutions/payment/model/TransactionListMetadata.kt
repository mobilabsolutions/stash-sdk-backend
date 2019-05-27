package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigInteger

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Transaction list metadata")
data class TransactionListMetadata(
    @ApiModelProperty(value = "Total transaction count")
    val totalCount: BigInteger?,

    @ApiModelProperty(value = "Returned transaction count")
    val pageCount: Int?,

    @ApiModelProperty(value = "Transaction list offset")
    val offset: Int?,

    @ApiModelProperty(value = "Transaction list limit")
    val limit: Int?
)
