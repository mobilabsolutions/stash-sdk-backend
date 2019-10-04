/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

@ApiModel(value = "Braintree refund request model")
data class BraintreeRefundRequestModel(
    @ApiModelProperty(value = "Braintree transaction id", example = "42949")
    @field:NotNull
    val pspTransactionId: String?,

    @ApiModelProperty(value = "Amount in decimal format", example = "5.00")
    @field:NotNull
    val amount: String?
)
