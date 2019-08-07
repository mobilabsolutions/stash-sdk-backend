/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.adyen.configuration.AdyenProperties
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.model.request.AdyenCaptureRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenDeleteAliasRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRefundRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenReverseRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenVerify3DSecureRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenVerifyPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.response.Adyen3DSecureResponseModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenPaymentResponseModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenVerifyPaymentResponseModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import mu.KLogging
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class AdyenClient(
    private val adyenProperties: AdyenProperties,
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging() {
        const val VERIFY_URL = "/payments/result"
        const val API_KEY = "X-API-Key"
        const val PAYLOAD = "payload"
        const val PREAUTH_URL = "/authorise"
        const val AUTHORIZATION_URL = "/authorise"
        const val PAYMENT_URL = "/payments"
        const val VERIFY_PAYMENT_URL = "/payments/details"
        const val REVERSE_URL = "/cancel"
        const val CAPTURE_URL = "/capture"
        const val REFUND_URL = "/refund"
        const val SEPA_REFUND_URL = "/cancelOrRefund"
        const val DELETE_ALIAS_URL = "/disable"
        const val ERROR_MESSAGE = "message"
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
        urlPrefix: String?,
        mode: String
    ): AdyenVerifyPaymentResponseModel {
        val verifyUrl =
            if (mode == AdyenMode.TEST.mode) adyenProperties.testCheckoutBaseUrl + VERIFY_URL
            else adyenProperties.liveCheckoutBaseUrl.format(urlPrefix) + VERIFY_URL

        val response = khttp.post(
            url = verifyUrl,
            headers = mapOf(API_KEY to verifyRequest.apiKey!!),
            json = mapOf(PAYLOAD to verifyRequest.payload)
        )

        return AdyenVerifyPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes a payment and registers a credit card with 3D Secure
     *
     * @param request Adyen payment request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen 3D secure response
     */
    fun registerThreeDSecure(
        request: AdyenPaymentRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): Adyen3DSecureResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testCheckoutBaseUrl + PAYMENT_URL
        else adyenProperties.liveCheckoutBaseUrl.format(pspConfig.urlPrefix) + PAYMENT_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )
        return Adyen3DSecureResponseModel(response.jsonObject)
    }

    /**
     * Verifies a payment for 3D Secure
     *
     * @param request Adyen verify payment request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen verify payment response
     */
    fun verifyThreeDSecure(
        request: AdyenVerify3DSecureRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenVerifyPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testCheckoutBaseUrl + VERIFY_PAYMENT_URL
        else adyenProperties.liveCheckoutBaseUrl.format(pspConfig.urlPrefix) + VERIFY_PAYMENT_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        return AdyenVerifyPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes authorization request to Adyen
     *
     * @param request Adyen authorization request
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
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + AUTHORIZATION_URL
        else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + AUTHORIZATION_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes preauthorization request to Adyen
     *
     * @param request Adyen preauthorization request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun preauthorization(
        request: AdyenPaymentRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + PREAUTH_URL
        else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + PREAUTH_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes capture request to Adyen
     *
     * @param request Adyen capture request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun capture(
        request: AdyenCaptureRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + CAPTURE_URL
        else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + CAPTURE_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes reversal request to Adyen
     *
     * @param request Adyen reverse request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun reverse(
        request: AdyenReverseRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + REVERSE_URL
        else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + REVERSE_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

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
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testCheckoutBaseUrl + PAYMENT_URL
        else adyenProperties.liveCheckoutBaseUrl.format(pspConfig.urlPrefix) + PAYMENT_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        if (HttpStatus.OK.value() != response.statusCode) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during authorizing Adyen payment")
                .withError(response.jsonObject.getString(ERROR_MESSAGE)).build().asException()
        }

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes refund request to Adyen
     *
     * @param request Adyen refund request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun refund(
        request: AdyenRefundRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + REFUND_URL
        else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + REFUND_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Makes SEPA refund request to Adyen
     *
     * @param request Adyen refund request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     * @return Adyen payment response
     */
    fun sepaRefund(
        request: AdyenRefundRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ): AdyenPaymentResponseModel {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testPaymentBaseUrl + SEPA_REFUND_URL
        else adyenProperties.livePaymentBaseUrl.format(pspConfig.urlPrefix) + SEPA_REFUND_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        return AdyenPaymentResponseModel(response.jsonObject)
    }

    /**
     * Deletes Alias at Adyen
     *
     * @param request Adyen delete alias request
     * @param pspConfig Adyen configuration
     * @param mode test or live mode
     */
    fun deleteAlias(
        request: AdyenDeleteAliasRequestModel,
        pspConfig: PspConfigModel,
        mode: String
    ) {
        val apiKey = if (mode == AdyenMode.TEST.mode) pspConfig.sandboxPublicKey else pspConfig.publicKey
        val paymentUrl = if (mode == AdyenMode.TEST.mode) adyenProperties.testRecurringBaseUrl + DELETE_ALIAS_URL
        else adyenProperties.liveRecurringBaseUrl.format(pspConfig.urlPrefix) + DELETE_ALIAS_URL

        val response = khttp.post(
            url = paymentUrl,
            headers = mapOf(API_KEY to apiKey!!),
            json = JSONObject(objectMapper.writeValueAsString(request))
        )

        if (HttpStatus.OK.value() != response.statusCode) {
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during deleting Adyen alias")
                .withError(response.jsonObject.getString(ERROR_MESSAGE)).build().asException()
        }
    }
}
