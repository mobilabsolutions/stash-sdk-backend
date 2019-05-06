package com.mobilabsolutions.payment.adyen.service

import com.adyen.Client
import com.adyen.Config
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.PaymentSessionRequest
import com.adyen.service.Checkout
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.Calendar
/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class AdyenClient(
    private val randomStringGenerator: RandomStringGenerator,
    private val jsonMapper: ObjectMapper,
    private val adyenProperties: AdyenProperties
) {
    companion object : KLogging() {
        const val STRING_LENGTH = 20
        const val VERIFY_URL = "/payments/result"
        const val API_KEY = "X-API-Key"
        const val PAYLOAD = "payload"
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        const val PREAUTH_URL = "/authorise"
    }

    /**
     * Requests an Adyen payment session
     *
     * @param pspConfigModel Adyen configuration
     * @param dynamicPspConfig Dynamic PSP configuration request
     * @return payment session
     */
    fun requestPaymentSession(pspConfigModel: PspConfigModel, dynamicPspConfig: DynamicPspConfigRequestModel, mode: String): String {
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
        paymentSessionRequest.channel = if (dynamicPspConfig.channel.equals(AdyenChannel.ANDROID.channel, ignoreCase = true))
            PaymentSessionRequest.ChannelEnum.ANDROID else PaymentSessionRequest.ChannelEnum.IOS
        paymentSessionRequest.enableRecurring(true)
        paymentSessionRequest.enableOneClick(true)
        paymentSessionRequest.token = dynamicPspConfig.token
        paymentSessionRequest.returnUrl = dynamicPspConfig.returnUrl
        paymentSessionRequest.countryCode = pspConfigModel.country
        paymentSessionRequest.shopperLocale = pspConfigModel.locale
        paymentSessionRequest.sessionValidity = SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().run {
            add(Calendar.MINUTE, 5)
            time
        }.time)
        paymentSessionRequest.merchantAccount = if (mode == AdyenMode.TEST.mode) pspConfigModel.sandboxMerchantId else pspConfigModel.merchantId
        paymentSessionRequest.amount = amount

        val response = try {
            checkout.paymentSession(paymentSessionRequest)
        } catch (exception: Exception) {
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Error during requesting Adyen payment session").asException()
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
    fun verifyPayment(verifyRequest: AdyenVerifyPaymentRequestModel, urlPrefix: String, mode: String): AdyenVerifyPaymentResponseModel? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(API_KEY, verifyRequest.apiKey)
        val verifyUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testCheckoutBaseUrl + VERIFY_URL else adyenProperties.liveCheckoutBaseUrl.format(urlPrefix) + VERIFY_URL

        val response = khttp.post(
            url = verifyUrl,
            headers = mapOf(API_KEY to verifyRequest.apiKey!!),
            json = mapOf(PAYLOAD to verifyRequest.payload))

        if (HttpStatus.OK.value() != response.statusCode) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during verifying Adyen payment session")
                .withError(response.jsonObject.getString("message")).build().asException()
        }
        return AdyenVerifyPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes preauthorization request to Adyen
     *
     * @param request Adyen payment request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun preauthorize(request: AdyenPaymentRequestModel, pspConfig: PspConfigModel, mode: String): AdyenPaymentResponseModel? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        if (mode == AdyenMode.TEST.mode) {
            //headers.setBasicAuth(pspConfig.sandboxUsername!!, pspConfig.sandboxPassword!!)
            val httpRequest = khttp.post(
                url = adyenProperties.testPaymentBaseUrl + PREAUTH_URL,
                headers = mapOf(API_KEY to pspConfig.sandboxPublicKey!!),
                json = jacksonObjectMapper().convertValue(request, object: TypeReference<Map<String, Any>>() {})
            )
            return AdyenPaymentResponseModel(httpRequest.jsonObject)
        }
        else{
            //headers.setBasicAuth(pspConfig.username!!, pspConfig.sandboxPassword!!)
            val httpRequest = khttp.post(
                url = adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + PREAUTH_URL,
                headers = mapOf(API_KEY to pspConfig.publicKey!!),
                json = jacksonObjectMapper().convertValue(request, object: TypeReference<Map<String, Any>>() {})
            )
            return AdyenPaymentResponseModel(httpRequest.jsonObject)
        }
    }
}
