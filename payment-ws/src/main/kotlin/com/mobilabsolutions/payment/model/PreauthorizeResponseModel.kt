package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Preauthorize Response Model")
data class PreauthorizeResponseModel(
    @ApiModelProperty("Transaction ID")
    val id: String?,

    @ApiModelProperty("Amount")
    val amount: Int,

    @ApiModelProperty("Currency")
    val currency: String?,

    @ApiModelProperty("Status")
    val status: TransactionStatus?,

    @ApiModelProperty("Action")
    val action: TransactionAction?
)