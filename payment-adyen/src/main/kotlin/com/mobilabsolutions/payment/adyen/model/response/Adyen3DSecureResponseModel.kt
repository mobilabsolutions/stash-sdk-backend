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

    @ApiModelProperty(value = "Payload needed when redirecting the shopper", example = "eNpVUl1zgjAQ/CvWH0ASkA+ZMzNUO...")
    val paReq: String?,

    @ApiModelProperty(value = "The returnUrl provided in the request", example = "https://payment-dev.mblb.net")
    val termUrl: String?,

    @ApiModelProperty(value = "Payload needed to verify the payment", example = "VkVneDFKL3NGbFlNZ05QM0VzeXZKQT09ISuR...")
    val md: String?,

    @ApiModelProperty(value = "URL to where the shopper should be redirected to complete a 3D Secure authentication", example = "https://test.adyen.com/hpp/3d/validate.shtml")
    val url: String?,

    @ApiModelProperty(value = "Adyen reason of refused payment", example = "Bad amount")
    val refusalReason: String?,

    @ApiModelProperty(value = "Adyen error message", example = "Bad amount")
    val errorMessage: String?
) {
    companion object {
        const val PAYMENT_DATA = "paymentData"
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
        jsonObject.getStringSafe(PAYMENT_DATA),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getJsonObjectSafe(DATA)?.getStringSafe(PA_REQ),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getJsonObjectSafe(DATA)?.getStringSafe(TERM_URL),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getJsonObjectSafe(DATA)?.getStringSafe(MD),
        jsonObject.getJsonObjectSafe(REDIRECT)?.getStringSafe(URL),
        jsonObject.getStringSafe(REFUSAL_REASON),
        jsonObject.getStringSafe(ERROR_MESSAGE)
    )
}
