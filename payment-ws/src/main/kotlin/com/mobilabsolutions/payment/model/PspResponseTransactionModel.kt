package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.TransactionStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("PSP Response Transaction Model")
data class PspResponseTransactionModel(

    @ApiModelProperty(value = "PSP Transaction ID", example = "199")
    val pspTransactionId: String?,

    @ApiModelProperty(value = "Transaction Status", example = "SUCCESS")
    val status: TransactionStatus?,

    @ApiModelProperty(value = "Customer ID", example = "192")
    val customerId: String?
)