package com.mobilabsolutions.payment.braintree.model.response

import com.mobilabsolutions.payment.data.enum.TransactionStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Braintree Payment Response")
data class BraintreePaymentResponseModel (
    @ApiModelProperty(value = "Braintree transaction status", example = "SUCCESS")
    val status: TransactionStatus?,

    @ApiModelProperty(value = "Braintree  transaction id")
    val transactionId: String?,

    @ApiModelProperty(value = "Braintree  customer id")
    val customerId: String?,

    @ApiModelProperty(value = "Braintree  error code")
    val errorCode: String?,

    @ApiModelProperty(value = "Braintree  error message")
    val errorMessage: String?
)
