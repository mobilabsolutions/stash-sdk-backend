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
data class Adyen3DSResponseModel(
    @ApiModelProperty(value = "Recurring detail reference", example = "8415568838266087")
    val recurringDetailReference: String?,

    @ApiModelProperty(value = "Shopper reference", example = "oIXHpTAfEPSleWXT6Khe")
    val shopperReference: String?,

    @ApiModelProperty(value = "Payload needed to verify the payment", example = "Ab02b4c0!BQABAgCYHYurjVnu8GRyhy1ZsGj...")
    val paymentData: String?,

    @ApiModelProperty(value = "Result code", example = "IdentifyShopper")
    val resultCode: String?,

    @ApiModelProperty(value = "Fingerprint token", example = "eyJ0aH...")
    val fingerprintToken: String?,

    @ApiModelProperty(value = "Challenge token", example = "eyJ0aH...")
    val challengeToken: String?,

    @ApiModelProperty(value = "Action type", example = "threeDS2Fingerprint")
    val type: String?,

    @ApiModelProperty(value = "Payment method type", example = "scheme")
    val paymentMethodType: String?,

    @ApiModelProperty(value = "Payload needed when redirecting the shopper", example = "eyJ0aH...")
    val paReq: String?,

    @ApiModelProperty(value = "The return url provided in the request", example = "https://payment-dev.mblb.net")
    val termUrl: String?,

    @ApiModelProperty(value = "Payload needed to complete the payment", example = "OEVudmZVMUlkWjd0MDNwUWs2bmhSdz09...")
    val md: String?,

    @ApiModelProperty(value = "Url where shopper will be redirected", example = "https://test.adyen.com/hpp/3d/validate.shtml")
    val url: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?,

    @ApiModelProperty(value = "Adyen error message", example = "Bad amount")
    val errorMessage: String?
) {
    companion object {
        const val ADDITIONAL_DATA = "additionalData"
        const val RECURRING_REFERENCE = "recurring.recurringDetailReference"
        const val SHOPPER_REFERENCE = "recurring.shopperReference"
        const val PAYMENT_DATA = "paymentData"
        const val RESULT_CODE = "resultCode"
        const val AUTHENTICATION = "authentication"
        const val FINGERPRINT_TOKEN = "threeds2.fingerprintToken"
        const val CHALLENGE_TOKEN = "threeds2.challengeToken"
        const val ACTION = "action"
        const val TYPE = "type"
        const val PAYMENT_METHOD_TYPE = "paymentMethodType"
        const val REDIRECT = "redirect"
        const val DATA = "data"
        const val PA_REQ = "PaReq"
        const val TERM_URL = "TermUrl"
        const val MD = "MD"
        const val URL = "url"
        const val REFUSAL_REASON = "refusalReason"
        const val ERROR_MESSAGE = "message"
    }

    constructor(jsonObject: JSONObject) : this(
        jsonObject.getJsonObjectSafe(ADDITIONAL_DATA)?.getStringSafe(RECURRING_REFERENCE),
        jsonObject.getJsonObjectSafe(ADDITIONAL_DATA)?.getStringSafe(SHOPPER_REFERENCE),
        jsonObject.getStringSafe(PAYMENT_DATA),
        jsonObject.getStringSafe(RESULT_CODE),
        jsonObject.getJsonObjectSafe(AUTHENTICATION)?.getStringSafe(FINGERPRINT_TOKEN),
        jsonObject.getJsonObjectSafe(AUTHENTICATION)?.getStringSafe(CHALLENGE_TOKEN),
        jsonObject.getJsonObjectSafe(ACTION)?.getStringSafe(TYPE),
        jsonObject.getJsonObjectSafe(ACTION)?.getStringSafe(PAYMENT_METHOD_TYPE),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getJsonObjectSafe(DATA)?.getStringSafe(PA_REQ),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getJsonObjectSafe(DATA)?.getStringSafe(TERM_URL),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getJsonObjectSafe(DATA)?.getStringSafe(MD),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getJsonObjectSafe(DATA)?.getStringSafe(URL),
        jsonObject.getStringSafe(REFUSAL_REASON),
        jsonObject.getStringSafe(ERROR_MESSAGE)
    )
}
