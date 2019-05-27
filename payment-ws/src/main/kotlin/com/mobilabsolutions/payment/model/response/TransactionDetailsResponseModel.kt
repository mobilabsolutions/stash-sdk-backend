package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.PaymentInfoModel
import com.mobilabsolutions.payment.model.request.TransactionTimelineModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Transaction Details Response")
data class TransactionDetailsResponseModel(
    @ApiModelProperty(value = "Transaction id", example = "frtdqw7m")
    val transactionId: String?,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currencyId: String?,

    @ApiModelProperty(value = "Amount in smallest currency unit (e.g. cent)", example = "2000")
    val amount: Int?,

    @ApiModelProperty(value = "Reason", example = "Payment for dinner")
    val reason: String?,

    @ApiModelProperty(value = "Transaction action")
    val action: String?,

    @ApiModelProperty(value = "Transaction status", example = "AUTHORIZED")
    val status: String?,

    @ApiModelProperty(value = "Payment method", example = "SEPA")
    val paymentMethod: String?,

    @ApiModelProperty(value = "Payment information")
    val paymentInfo: PaymentInfoModel?,

    @ApiModelProperty(value = "Merchant transaction id", example = "frtdqw7m")
    val merchantTransactionId: String?,

    @ApiModelProperty(value = "Merchant customer id", example = "frefatdfqw7am")
    val merchantCustomerId: String?,

    @ApiModelProperty(value = "PSP test mode", example = "true")
    val pspTestMode: Boolean?,

    @ApiModelProperty(value = "Merchant id", example = "Mobilab")
    val merchantId: String?,

    @ApiModelProperty(value = "Alias id", example = "sswe34wdff")
    val aliasId: String?,

    @ApiModelProperty(value = "Timeline information")
    val timelineInfo: MutableList<TransactionTimelineModel> = mutableListOf()
)
