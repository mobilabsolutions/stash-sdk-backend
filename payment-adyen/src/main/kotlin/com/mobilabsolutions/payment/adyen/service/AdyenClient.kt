package com.mobilabsolutions.payment.adyen.service

import com.adyen.Client
import com.adyen.Config
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.PaymentSessionRequest
import com.adyen.service.Checkout
import com.mobilabsolutions.payment.adyen.data.enum.AdyenChannel
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
class AdyenClient(
    private val randomStringGenerator: RandomStringGenerator
) {
    companion object : KLogging() {
        const val STRING_LENGTH = 20
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
}
