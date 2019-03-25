package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.server.commons.exception.ApiException
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Authorization Response")
data class PspPaymentResponseModel(
    @ApiModelProperty(value = "PSP transaction id", example = "jdhcjdhc")
    val pspTransactionId: String?,

    @ApiModelProperty("Transaction status", example = "SUCCESS")
    val status: TransactionStatus?,

    @ApiModelProperty(value = "BS Payone customer id")
    val customerId: String?,

    @ApiModelProperty(value = "API exception")
    val apiException: ApiException?
) {
    fun hasError() = apiException != null
}