/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.validation.TransactionActionEnumValidator
import com.mobilabsolutions.payment.validation.TransactionStatusEnumValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "PSP Notification Model")
data class PspNotificationModel(
    @ApiModelProperty("PSP Transaction Id", example = "rtHyGsuShuKMn")
    val pspTransactionId: String?,

    @ApiModelProperty("Payment data")
    @field:Valid
    val paymentData: PaymentDataRequestModel?,

    @ApiModelProperty(value = "Transaction action", example = "Values: PREAUTH, AUTH, REVERSAL, REFUND, CAPTURE")
    @field:TransactionActionEnumValidator(TransactionAction = TransactionAction::class)
    @field:NotNull
    val transactionAction: String?,

    @ApiModelProperty(value = "Transaction status", example = "Values: SUCCESS, FAIL")
    @field:TransactionStatusEnumValidator(TransactionStatus = TransactionStatus::class)
    @field:NotNull
    val transactionStatus: String?
)
