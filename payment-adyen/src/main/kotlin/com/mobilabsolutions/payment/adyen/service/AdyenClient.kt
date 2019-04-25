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
        const val TEST_URL = "https://pal-test.adyen.com"
        const val LIVE_URL = "https://random-mobilabsolutions-pal-live.adyenpayments.com"
    }

    /**
     * Returns Adyen client token.
     *
     * @param pspConfigModel Adyen configuration
     * @return payment session
     */
    fun generateClientToken(pspConfigModel: PspConfigModel, mode: String): String {
        val config = Config()
        config.apiKey = if (mode == AdyenMode.TEST.mode) pspConfigModel.sandboxPublicKey else pspConfigModel.publicKey

        val client = Client(config)
        if (mode == AdyenMode.TEST.mode) client.setEnvironment(Environment.TEST, TEST_URL)
        else client.setEnvironment(Environment.LIVE, LIVE_URL)

        val checkout = Checkout(client)

        val amount = Amount()
        amount.currency = pspConfigModel.currency
        amount.value = 0

        val paymentSessionRequest = PaymentSessionRequest()
        paymentSessionRequest.reference = randomStringGenerator.generateRandomAlphanumeric(5)
        paymentSessionRequest.shopperReference = randomStringGenerator.generateRandomAlphanumeric(STRING_LENGTH)
        paymentSessionRequest.channel = if (pspConfigModel.dynamicPspConfig!!.channel == AdyenChannel.ANDROID.channel) PaymentSessionRequest.ChannelEnum.ANDROID else PaymentSessionRequest.ChannelEnum.IOS
        paymentSessionRequest.token = pspConfigModel.dynamicPspConfig!!.token
        paymentSessionRequest.returnUrl = pspConfigModel.dynamicPspConfig!!.returnUrl
        paymentSessionRequest.countryCode = pspConfigModel.country
        paymentSessionRequest.shopperLocale = pspConfigModel.locale
        paymentSessionRequest.sessionValidity = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        paymentSessionRequest.merchantAccount = if (mode == AdyenMode.TEST.mode) pspConfigModel.sandboxMerchantId else pspConfigModel.merchantId
        paymentSessionRequest.amount = amount

        val response = checkout.paymentSession(paymentSessionRequest)

        return response.paymentSession.toString()
    }
}
