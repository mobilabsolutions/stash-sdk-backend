/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.response

import com.mobilabsolutions.payment.adyen.configuration.getStringSafe
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.json.JSONObject

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen Payment Response")
data class AdyenPaymentResponseModel(
    @ApiModelProperty(value = "PSP reference", example = "kdcnvbfkhbvka")
    val pspReference: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?,

    @ApiModelProperty(value = "Adyen error message", example = "Bad amount")
    val errorMessage: String?
) {
    companion object {
        const val PSP_REFERENCE = "pspReference"
        const val REFUSAL_REASON = "refusalReason"
        const val ERROR_MESSAGE = "message"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getStringSafe(PSP_REFERENCE),
        jsonObject.getStringSafe(REFUSAL_REASON),
        jsonObject.getStringSafe(ERROR_MESSAGE)
    )
}
