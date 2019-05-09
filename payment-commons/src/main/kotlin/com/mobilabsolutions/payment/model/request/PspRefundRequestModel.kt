package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.model.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Refund Request")
data class PspRefundRequestModel(
    @ApiModelProperty(value = "PSP transaction id", example = "42949")
    val pspTransactionId: String?,

    @ApiModelProperty(value = "Amount in smallest currency unit (e.g. cent)", example = "500")
    val amount: Int?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currency: String?,

    @ApiModelProperty(value = "Transaction action", example = "CAPTURE")
    @field:Enumerated(EnumType.STRING)
    val action: TransactionAction?,

    @ApiModelProperty(value = "PSP config")
    val pspConfig: PspConfigModel?,

    @ApiModelProperty("Purchase ID", example = "132")
    val purchaseId: String?,

    @ApiModelProperty(value = "Payment method", example = "SEPA")
    @field:Enumerated(EnumType.STRING)
    val paymentMethod: PaymentMethod?
)
