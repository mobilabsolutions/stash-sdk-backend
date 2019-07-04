/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Payload Request Model")
data class PayloadRequestModel(
    @ApiModelProperty(value = "Adyen Payload")
    val payload: String?
)
