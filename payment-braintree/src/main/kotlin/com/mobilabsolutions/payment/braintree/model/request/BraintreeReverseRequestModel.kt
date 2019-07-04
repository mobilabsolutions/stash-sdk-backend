/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

@ApiModel(value = "Braintree Reverse Request")
data class BraintreeReverseRequestModel(
    @ApiModelProperty(value = "Braintree transaction id", example = "42949")
    @field:NotNull
    val pspTransactionId: String?
)
