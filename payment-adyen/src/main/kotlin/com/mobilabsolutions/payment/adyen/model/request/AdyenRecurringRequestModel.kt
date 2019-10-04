/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen recurring request model")
data class AdyenRecurringRequestModel(
    @ApiModelProperty(value = "Adyen contract", example = "RECURRING")
    val contract: String?
)
