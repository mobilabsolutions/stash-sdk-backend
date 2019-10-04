/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.model.response

import com.braintreegateway.Transaction
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Braintree payment response model")
data class BraintreePaymentResponseModel(
    @ApiModelProperty(value = "Braintree response status", example = "AUTHORIZED")
    val status: Transaction.Status?,

    @ApiModelProperty(value = "Braintree  transaction id", example = "frtdqw7m")
    val transactionId: String?,

    @ApiModelProperty(value = "Braintree  error code")
    val errorCode: String? = null,

    @ApiModelProperty(value = "Braintree  error message")
    val errorMessage: String? = null
)
