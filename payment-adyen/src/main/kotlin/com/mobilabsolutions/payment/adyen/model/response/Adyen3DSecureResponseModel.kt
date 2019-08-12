package com.mobilabsolutions.payment.adyen.model.response

import com.mobilabsolutions.payment.adyen.configuration.getJsonObjectSafe
import com.mobilabsolutions.payment.adyen.configuration.getStringSafe
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.json.JSONObject

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen 3D Secure Response")
data class Adyen3DSecureResponseModel(
    @ApiModelProperty(value = "Payload needed to verify the payment", example = "Ab02b4c0!BQABAgCYHYurjVnu8GRyhy1ZsGj...")
    val paymentData: String?,

    val resultCode: String?,

    val fingerprintToken: String?,

    val challengeToken: String?,

    val type: String?,

    val paymentMethodType: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?,

    @ApiModelProperty(value = "Adyen error message", example = "Bad amount")
    val errorMessage: String?
) {
    companion object {
        const val PAYMENT_DATA = "paymentData"
        const val RESULT_CODE = "resultCode"
        const val AUTHENTICATION = "authentication"
        const val FINGERPRINT_TOKEN = "threeds2.fingerprintToken"
        const val CHALLENGE_TOKEN = "threeds2.challengeToken"
        const val ACTION = "action"
        const val TYPE = "type"
        const val PAYMENT_METHOD_TYPE = "paymentMethodType"
        const val REFUSAL_REASON = "refusalReason"
        const val ERROR_MESSAGE = "message"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getStringSafe(PAYMENT_DATA),
        jsonObject.getStringSafe(RESULT_CODE),
        jsonObject.getJsonObjectSafe(AUTHENTICATION)?.getStringSafe(FINGERPRINT_TOKEN),
        jsonObject.getJsonObjectSafe(AUTHENTICATION)?.getStringSafe(CHALLENGE_TOKEN),
        jsonObject.getJsonObjectSafe(ACTION)?.getStringSafe(TYPE),
        jsonObject.getJsonObjectSafe(ACTION)?.getStringSafe(PAYMENT_METHOD_TYPE),
        jsonObject.getStringSafe(REFUSAL_REASON),
        jsonObject.getStringSafe(ERROR_MESSAGE)
    )
}
