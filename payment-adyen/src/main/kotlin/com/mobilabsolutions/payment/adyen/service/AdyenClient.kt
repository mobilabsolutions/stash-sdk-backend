package com.mobilabsolutions.payment.adyen.service

import com.adyen.Client
import com.adyen.Config
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.PaymentSessionRequest
import com.adyen.service.Checkout
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import com.mobilabsolutions.payment.adyen.data.enum.AdyenChannel
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.model.request.AdyenVerifyPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.request.PayloadRequestModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenVerifyPaymentResponseModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import mu.KLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class AdyenClient(
    private val randomStringGenerator: RandomStringGenerator,
    private val restTemplate: RestTemplate,
    private val jsonMapper: ObjectMapper
    ) {
    companion object : KLogging() {
        const val STRING_LENGTH = 20
        const val VERIFY_URL = "https://checkout-test.adyen.com/v40/payments/result"
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
        if (mode == AdyenMode.TEST.mode) client.setEnvironment(Environment.TEST, pspConfigModel.sandboxServerUrl)
        else client.setEnvironment(Environment.LIVE, pspConfigModel.serverUrl)

        val checkout = Checkout(client)

        val amount = Amount()
        amount.currency = pspConfigModel.currency
        amount.value = 0

        val paymentSessionRequest = PaymentSessionRequest()
        paymentSessionRequest.reference = randomStringGenerator.generateRandomAlphanumeric(STRING_LENGTH)
        paymentSessionRequest.shopperReference = randomStringGenerator.generateRandomAlphanumeric(STRING_LENGTH)
        paymentSessionRequest.channel = if (dynamicPspConfig.channel.equals(AdyenChannel.ANDROID.channel, ignoreCase = true)) PaymentSessionRequest.ChannelEnum.ANDROID else PaymentSessionRequest.ChannelEnum.IOS
        paymentSessionRequest.token = dynamicPspConfig.token
        paymentSessionRequest.returnUrl = dynamicPspConfig.returnUrl
        paymentSessionRequest.countryCode = pspConfigModel.country
        paymentSessionRequest.shopperLocale = pspConfigModel.locale
        paymentSessionRequest.sessionValidity = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        paymentSessionRequest.merchantAccount = if (mode == AdyenMode.TEST.mode) pspConfigModel.sandboxMerchantId else pspConfigModel.merchantId
        paymentSessionRequest.amount = amount

        val response = try {
            checkout.paymentSession(paymentSessionRequest)
        } catch (exception: Exception) {
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Error during requesting Adyen payment session").asException()
        }

        return response.paymentSession.toString()
    }

    /**
     * Verifies Adyen payment result
     *
     * @param verifyRequest Adyen verify payment request
     * @return Adyen verify payment response
     */
    fun verifyPayment(verifyRequest: AdyenVerifyPaymentRequestModel): AdyenVerifyPaymentResponseModel {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.add("X-API-Key", verifyRequest.apiKey)
        val request = HttpEntity(PayloadRequestModel(verifyRequest.payload), headers)

        val response = restTemplate.postForEntity(VERIFY_URL, request, String::class.java)
        return convertToResponse(response.body!!, AdyenVerifyPaymentResponseModel::class.java)
    }

    /**
     * Converts Adyen response body to internal response.
     *
     * @param body response body
     * @param response response class
     * @return internal response
     */
    private fun <T> convertToResponse(body: String, response: Class<T>): T {
        val params = body.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val map = params.map { s ->
            val parts = s.split("=".toRegex(), 2).toTypedArray()
            Maps.immutableEntry(parts[0], parts[1])
        }.map { it.key to it.value }.toMap()
        return jsonMapper.convertValue(map, response)
    }
}
