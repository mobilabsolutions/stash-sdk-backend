package com.mobilabsolutions.payment.adyen.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.json.JSONObject

@ApiModel(value = "Adyen Payment Response")
data class AdyenPaymentResponseModel(
//    @ApiModelProperty(value = "Authorisation code", example = "123")
//    val authCode: String?,

    @ApiModelProperty(value = "PSP reference", example = "sje324andls")
    val pspReference: String?

//    @ApiModelProperty(value = "Refusal reason", example = "Failed transaction")
//    val refusalReason: String?,

//    @ApiModelProperty(value = "Result code", example = "Authorised")
//    val resultCode: String?
) {
    companion object {
        const val PSP_REFERENCE = "pspReference"
        //const val REFUSAL_REASON = "refusalReason"
        const val RESULT_CODE = "resultCode"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getString(PSP_REFERENCE)
        //jsonObject.getString(REFUSAL_REASON),
        //jsonObject.getString(RESULT_CODE)
    )
}
