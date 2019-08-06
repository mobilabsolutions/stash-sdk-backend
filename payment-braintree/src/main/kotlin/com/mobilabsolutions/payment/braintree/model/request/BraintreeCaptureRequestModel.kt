/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Braintree Capture Request")
data class BraintreeCaptureRequestModel(
    @ApiModelProperty(value = "Braintree transaction id", example = "42949")
    @field:NotNull
    val pspTransactionId: String?
)
