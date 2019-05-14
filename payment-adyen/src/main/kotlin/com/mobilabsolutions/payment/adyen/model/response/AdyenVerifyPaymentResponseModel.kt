package com.mobilabsolutions.payment.adyen.model.response

import com.mobilabsolutions.payment.adyen.configuration.getJsonObjectSafe
import com.mobilabsolutions.payment.adyen.configuration.getStringSafe
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.json.JSONObject

@ApiModel(value = "Adyen Verify Payment Response")
data class AdyenVerifyPaymentResponseModel(
    @ApiModelProperty(value = "Recurring detail reference", example = "8415568838266087")
    val recurringDetailReference: String?,

    @ApiModelProperty(value = "Shopper reference", example = "oIXHpTAfEPSleWXT6Khe")
    val shopperReference: String?,

    @ApiModelProperty(value = "Adyen error message", example = "Bad amount")
    val errorMessage: String?
) {
    companion object {
        const val ADDITIONAL_DATA = "additionalData"
        const val RECURRING_REFERENCE = "recurring.recurringDetailReference"
        const val SHOPPER_REFERENCE = "recurring.shopperReference"
        const val ERROR_MESSAGE = "message"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getJsonObjectSafe(ADDITIONAL_DATA)?.getStringSafe(RECURRING_REFERENCE),
        jsonObject.getStringSafe(ERROR_MESSAGE),
        jsonObject.getJsonObjectSafe(ADDITIONAL_DATA)?.getStringSafe(SHOPPER_REFERENCE)
    )
}
