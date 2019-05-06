package com.mobilabsolutions.payment.adyen.model.response

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
    val pspReference: String?
) {
    companion object {
        const val ADDITIONAL_DATA = "additionalData"
        const val RECURRING_REFERENCE = "recurring.recurringDetailReference"
        const val PSP_REFERENCE = "pspReference"
        const val RESULT_CODE = "resultCode"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getJSONObject(ADDITIONAL_DATA).getString(RECURRING_REFERENCE),
        jsonObject.getString(RESULT_CODE),
        jsonObject.getString(PSP_REFERENCE)
    )
}
