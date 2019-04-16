package com.mobilabsolutions.payment.braintree.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import com.braintreegateway.Transaction

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Braintree Payment Response")
data class BraintreePaymentResponseModel(
    @ApiModelProperty(value = "Braintree response status", example = "SUCCESS")
    val status: Transaction.Status?,

    @ApiModelProperty(value = "Braintree  transaction id", example = "frtdqw7m")
    val transactionId: String?,

    @ApiModelProperty(value = "Braintree  error code")
    val errorCode: String? = null,

    @ApiModelProperty(value = "Braintree  error message")
    val errorMessage: String? = null
)
