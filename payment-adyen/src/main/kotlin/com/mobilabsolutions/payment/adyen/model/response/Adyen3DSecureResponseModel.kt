package com.mobilabsolutions.payment.adyen.model.response

import com.mobilabsolutions.payment.adyen.configuration.getJsonObjectSafe
import com.mobilabsolutions.payment.adyen.configuration.getStringSafe
import io.swagger.annotations.ApiModelProperty
import org.json.JSONObject

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
data class Adyen3DSecureResponseModel(
    val paymentData: String?,

    val paReq: String?,

    val termUrl: String?,

    val md: String?,

    val url: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?,

    @ApiModelProperty(value = "Adyen error message", example = "Bad amount")
    val errorMessage: String?
) {
    companion object {
        const val PAYMENT_DATA = "paymentData"
        const val REDIRECT = "redirect"
        const val PA_REQ = "data.PeReq"
        const val TERM_URL = "data.TermUrl"
        const val MD = "data.MD"
        const val URL = "url"
        const val REFUSAL_REASON = "refusalReason"
        const val ERROR_MESSAGE = "message"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getStringSafe(PAYMENT_DATA),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getStringSafe(PA_REQ),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getStringSafe(TERM_URL),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getStringSafe(MD),
        jsonObject.getStringSafe(URL),
        jsonObject.getStringSafe(REFUSAL_REASON),
        jsonObject.getStringSafe(ERROR_MESSAGE)
    )
}
