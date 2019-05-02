package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Adyen Recurring Request Model")
data class AdyenRecurringRequestModel(
    @ApiModelProperty(value = "Adyen contract", example = "RECURRING")
    val contract: String?
)
