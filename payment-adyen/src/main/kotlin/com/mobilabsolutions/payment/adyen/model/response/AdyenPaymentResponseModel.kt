package com.mobilabsolutions.payment.adyen.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.json.JSONException
import org.json.JSONObject

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen Payment Response")
data class AdyenPaymentResponseModel(
    @ApiModelProperty(value = "PSP reference", example = "kdcnvbfkhbvka")
    val pspReference: String?,

    @ApiModelProperty(value = "Adyen result code", example = "Authorised")
    val resultCode: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?
) {
    companion object {
        const val REFUSAL_REASON = "refusalReason"
        const val PSP_REFERENCE = "pspReference"
        const val RESULT_CODE = "resultCode"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getStringSafe(PSP_REFERENCE),
        jsonObject.getStringSafe(RESULT_CODE),
        jsonObject.getStringSafe(REFUSAL_REASON)
    )
}

private fun JSONObject.getStringSafe(key: String): String? {
    return try { this.getString(key) } catch (e: JSONException) { null }
}
