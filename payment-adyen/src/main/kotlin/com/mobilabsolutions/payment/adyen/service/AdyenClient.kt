package com.mobilabsolutions.payment.adyen.service

import com.adyen.Client
import com.adyen.Config
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.PaymentSessionRequest
import com.adyen.service.Checkout
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
    }

    /**
     * Returns Adyen client token.
     *
     * @param pspConfigModel Adyen configuration
     * @return payment session
     */
    fun generateClientToken(pspConfigModel: PspConfigModel): String {
        val config = Config()
        config.apiKey = pspConfigModel.sandboxPublicKey

        val client = Client(config)
        client.setEnvironment(Environment.TEST, "https://pal-test.adyen.com")

        val checkout = Checkout(client)

        val amount = Amount()
        amount.currency = "EUR"
        amount.value = 0

        val paymentSessionRequest = PaymentSessionRequest()
        paymentSessionRequest.reference = randomStringGenerator.generateRandomAlphanumeric(5)
        paymentSessionRequest.shopperReference = randomStringGenerator.generateRandomAlphanumeric(STRING_LENGTH)
        paymentSessionRequest.channel = PaymentSessionRequest.ChannelEnum.ANDROID
        paymentSessionRequest.token = pspConfigModel.dynamicPspConfig!!.token
        paymentSessionRequest.returnUrl = pspConfigModel.dynamicPspConfig!!.returnUrl
        paymentSessionRequest.countryCode = "DE"
        paymentSessionRequest.shopperLocale = "de_DE"
        paymentSessionRequest.sessionValidity = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        paymentSessionRequest.merchantAccount = pspConfigModel.sandboxMerchantId
        paymentSessionRequest.amount = amount

        val response = checkout.paymentSession(paymentSessionRequest)

        return response.paymentSession.toString()
    }
}
