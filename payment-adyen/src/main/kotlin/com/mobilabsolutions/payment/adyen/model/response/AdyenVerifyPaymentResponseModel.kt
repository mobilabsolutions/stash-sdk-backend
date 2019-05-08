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

    @ApiModelProperty(value = "Result code", example = "Authorised")
    val resultCode: String?,

    @ApiModelProperty(value = "PSP reference", example = "sje324andls")
    val pspReference: String?,

    @ApiModelProperty(value = "Shopper reference", example = "oIXHpTAfEPSleWXT6Khe")
    val shopperReference: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?
) {
    companion object {
        const val ADDITIONAL_DATA = "additionalData"
        const val RECURRING_REFERENCE = "recurring.recurringDetailReference"
        const val SHOPPER_REFERENCE = "recurring.shopperReference"
        const val PSP_REFERENCE = "pspReference"
        const val RESULT_CODE = "resultCode"
        const val REFUSAL_REASON = "refusalReason"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getJsonObjectSafe(ADDITIONAL_DATA)?.getStringSafe(RECURRING_REFERENCE),
        jsonObject.getStringSafe(RESULT_CODE),
        jsonObject.getStringSafe(PSP_REFERENCE),
        jsonObject.getJsonObjectSafe(ADDITIONAL_DATA)?.getStringSafe(SHOPPER_REFERENCE),
        jsonObject.getStringSafe(REFUSAL_REASON)
    )
}
