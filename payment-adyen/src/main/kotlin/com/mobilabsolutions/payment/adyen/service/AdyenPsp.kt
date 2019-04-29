package com.mobilabsolutions.payment.adyen.service

import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.model.request.AdyenVerifyPaymentRequestModel
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspDeleteAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
import com.mobilabsolutions.payment.model.response.PspPaymentResponseModel
import com.mobilabsolutions.payment.model.response.PspRegisterAliasResponseModel
import com.mobilabsolutions.payment.service.Psp
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class AdyenPsp(private val adyenClient: AdyenClient) : Psp {

    companion object : KLogging()

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.ADYEN
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?, dynamicPspConfig: DynamicPspConfigRequestModel?, pspTestMode: Boolean?): PspAliasConfigModel? {
        logger.info { "Adyen config calculation has been called..." }
        val adyenMode = getAdyenMode(pspTestMode)
        if (dynamicPspConfig == null) throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Missing dynamic Adyen configuration").asException()
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.ADYEN.toString(),
            merchantId = if (adyenMode == AdyenMode.TEST.mode) pspConfigModel.sandboxMerchantId else pspConfigModel.merchantId,
            portalId = null,
            request = null,
            apiVersion = null,
            responseType = null,
            hash = null,
            accountId = null,
            encoding = null,
            mode = adyenMode,
            publicKey = if (adyenMode == AdyenMode.TEST.mode) pspConfigModel.sandboxPublicKey else pspConfigModel.publicKey,
            privateKey = null,
            clientToken = null,
            paymentSession = adyenClient.requestPaymentSession(pspConfigModel, dynamicPspConfig, adyenMode)
        ) else null
    }

    override fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        if (pspRegisterAliasRequestModel.aliasExtra == null) throw ApiError.ofErrorCode(ApiErrorCode.INCOMPLETE_ALIAS).asException()
        val adyenMode = getAdyenMode(pspTestMode)
        val request = AdyenVerifyPaymentRequestModel(
            apiKey = if (adyenMode == AdyenMode.TEST.mode) pspRegisterAliasRequestModel.pspConfig!!.sandboxPublicKey else pspRegisterAliasRequestModel.pspConfig!!.publicKey,
            payload = pspRegisterAliasRequestModel.aliasExtra!!.payload,
            sandboxCheckoutUrl = pspRegisterAliasRequestModel.pspConfig!!.sandboxServerUrl,
            checkoutUrl = pspRegisterAliasRequestModel.pspConfig!!.serverUrl
        )

        val response = adyenClient.verifyPayment(request, getAdyenMode(pspTestMode))

        // To change what is passed to the response model when we have a correct payload
        return PspRegisterAliasResponseModel(response.message, response.message)
    }

    override fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun capture(pspCaptureRequestModel: PspCaptureRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Resolves PSP mode using passed in boolean
     *
     * @param test Boolean representing mode
     * @return Adyen mode as a string representation
     */
    private fun getAdyenMode(test: Boolean?): String {
        if (test == null || test == false) return AdyenMode.LIVE.mode
        return AdyenMode.TEST.mode
    }
}
