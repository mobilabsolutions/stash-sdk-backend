package com.mobilabsolutions.payment.adyen.service

import com.adyen.Client
import com.adyen.Config
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.PaymentSessionRequest
import com.adyen.service.Checkout
import com.adyen.service.exception.ApiException
import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.adyen.configuration.AdyenProperties
import com.mobilabsolutions.payment.adyen.data.enum.AdyenChannel
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenPaymentResponseModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenVerifyPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenVerifyPaymentResponseModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import mu.KLogging
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.Calendar
/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class AdyenClient(
    private val randomStringGenerator: RandomStringGenerator,
    private val adyenProperties: AdyenProperties,
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging() {
        const val STRING_LENGTH = 20
        const val VERIFY_URL = "/payments/result"
        const val API_KEY = "X-API-Key"
        const val PAYLOAD = "payload"
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        const val PREAUTH_URL = "/authorise"
        const val AUTHORIZATION_URL = "/authorise"
        const val SEPA_PAYMENT_URL = "/payments"
    }

    /**
     * Requests an Adyen payment session
     *
     * @param pspConfigModel Adyen configuration
     * @param dynamicPspConfig Dynamic PSP configuration request
     * @return payment session
     */
    fun requestPaymentSession(
        pspConfigModel: PspConfigModel,
        dynamicPspConfig: DynamicPspConfigRequestModel?,
        mode: String
    ): String {
        val config = Config()
        config.apiKey = if (mode == AdyenMode.TEST.mode) pspConfigModel.sandboxPublicKey else pspConfigModel.publicKey

        val client = Client(config)
        if (mode == AdyenMode.TEST.mode) client.setEnvironment(Environment.TEST, null)
        else client.setEnvironment(Environment.LIVE, pspConfigModel.urlPrefix)

        val checkout = Checkout(client)
        val amount = Amount()
        amount.currency = pspConfigModel.currency
        amount.value = 0

        val paymentSessionRequest = PaymentSessionRequest()
        paymentSessionRequest.reference = randomStringGenerator.generateRandomAlphanumeric(STRING_LENGTH)
        paymentSessionRequest.shopperReference = randomStringGenerator.generateRandomAlphanumeric(STRING_LENGTH)
        paymentSessionRequest.channel =
            if (dynamicPspConfig?.channel.equals(AdyenChannel.ANDROID.channel, ignoreCase = true))
                PaymentSessionRequest.ChannelEnum.ANDROID else PaymentSessionRequest.ChannelEnum.IOS
        paymentSessionRequest.enableRecurring(true)
        paymentSessionRequest.enableOneClick(true)
        paymentSessionRequest.token = dynamicPspConfig?.token
        paymentSessionRequest.returnUrl = dynamicPspConfig?.returnUrl
        paymentSessionRequest.countryCode = pspConfigModel.country
        paymentSessionRequest.shopperLocale = pspConfigModel.locale
        paymentSessionRequest.sessionValidity = SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().run {
            add(Calendar.MINUTE, 5)
            time
        }.time)
        paymentSessionRequest.merchantAccount =
            if (mode == AdyenMode.TEST.mode) pspConfigModel.sandboxMerchantId else pspConfigModel.merchantId
        paymentSessionRequest.amount = amount

        val response = try {
            checkout.paymentSession(paymentSessionRequest)
        } catch (exception: ApiException) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withError(exception.error.message)
                .withMessage("Error during requesting Adyen payment session").build().asException()
        }

        return response.paymentSession.toString()
    }

    /** Verifies Adyen payment result
     *
     * @param verifyRequest Adyen verify payment request
     * @param urlPrefix URL prefix
     * @param mode test or live mode
     * @return Adyen verify payment response
     */
    fun verifyPayment(
        verifyRequest: AdyenVerifyPaymentRequestModel,
        urlPrefix: String,
        mode: String
    ): AdyenVerifyPaymentResponseModel {
        val verifyUrl =
            if (mode == AdyenMode.TEST.mode) adyenProperties.testCheckoutBaseUrl + VERIFY_URL else adyenProperties.liveCheckoutBaseUrl.format(
                urlPrefix
            ) + VERIFY_URL

        val response = khttp.post(
            url = verifyUrl,
            headers = mapOf(API_KEY to verifyRequest.apiKey!!),
            json = mapOf(PAYLOAD to verifyRequest.payload)
        )

        if (HttpStatus.OK.value() != response.statusCode) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during verifying Adyen payment session")
                .withError(response.jsonObject.getString("message")).build().asException()
        }

        return AdyenVerifyPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes authorization request to Adyen
     *
     * @param request Adyen payment request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun authorization(
        request: AdyenPaymentRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + AUTHORIZATION_URL else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + AUTHORIZATION_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        if (HttpStatus.OK.value() != response.statusCode) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during authorizing Adyen payment")
                .withError(response.jsonObject.getString("message")).build().asException()
        }

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes preauthorization request to Adyen
     *
     * @param request Adyen payment request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun preauthorization(
        request: AdyenPaymentRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel? {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + PREAUTH_URL else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + PREAUTH_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        if (HttpStatus.OK.value() != response.statusCode) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during preauthorizing Adyen payment")
                .withError(response.jsonObject.getString("message")).build().asException()
        }

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes SEPA payment request to Adyen
     *
     * @param request Adyen payment request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun sepaPayment(
        request: AdyenPaymentRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testCheckoutBaseUrl + SEPA_PAYMENT_URL else adyenProperties.liveCheckoutBaseUrl.format(pspConfig.urlPrefix) + SEPA_PAYMENT_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        if (HttpStatus.OK.value() != response.statusCode) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during authorizing Adyen payment")
                .withError(response.jsonObject.getString("message")).build().asException()
        }

        return AdyenPaymentResponseModel(response.jsonObject)
    }
}
