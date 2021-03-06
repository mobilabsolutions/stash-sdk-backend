/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel("Payment response model")
data class PaymentResponseModel(
    @ApiModelProperty("Transaction ID")
    val id: String?,

    @ApiModelProperty("Amount")
    val amount: Int?,

    @ApiModelProperty("Currency")
    val currency: String?,

    @ApiModelProperty("Transaction status")
    val status: TransactionStatus?,

    @ApiModelProperty("Transaction action")
    val action: TransactionAction?,

    @ApiModelProperty("Additional info")
    val additionalInfo: String? = null
)
