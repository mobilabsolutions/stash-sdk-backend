package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Authorize Response Model")
data class AuthorizeResponseModel(
    @ApiModelProperty("Transaction ID")
    val id: String?,

    @ApiModelProperty("Amount")
    @field:NotNull
    val amount: Int,

    @ApiModelProperty("Currency")
    val currency: String?,

    @ApiModelProperty("Status")
    val status: TransactionStatus?,

    @ApiModelProperty("Action")
    val action: TransactionAction?
)