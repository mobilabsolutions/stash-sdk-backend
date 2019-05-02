package com.mobilabsolutions.payment.adyen.service

import com.mobilabsolutions.payment.adyen.model.request.AdyenVerifyPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenVerifyPaymentResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.adyen.configuration.AdyenProperties
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdyenPspTest {
    private val sandboxMerchantId = "some merchant id"
    private val sandboxPublicKey = "some public key"
    private val currency = "EUR"
    private val country = "DE"
    private val locale = "de-DE"
    private val urlPrefix = "random-mobilab"
    private val dynamicPspConfig = DynamicPspConfigRequestModel(
        "some token",
        "some url",
        "some channel"
    )
    private val pspConfig = PspConfigModel(
        PaymentServiceProvider.ADYEN.toString(),
        null,
        null,
        null,
        null,
        sandboxMerchantId,
        sandboxPublicKey,
        null,
        null,
        null,
        true,
        currency,
        country,
        locale,
        urlPrefix,
        null,
        null,
        null,
        null
    )
    private val paymentSession = "123"
    private val correctAliasId = "correct id"
    private val correctPayload = "payload"
    private val verifyRequest = AdyenVerifyPaymentRequestModel(
        sandboxPublicKey,
        correctPayload
    )

    @InjectMocks
    private lateinit var adyenPsp: AdyenPsp

    @Mock
    private lateinit var adyenClient: AdyenClient

    @Mock
    private lateinit var adyenProperties: AdyenProperties

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(adyenClient.requestPaymentSession(pspConfig, dynamicPspConfig, "test"))
            .thenReturn(paymentSession)
        Mockito.`when`(adyenClient.verifyPayment(verifyRequest, urlPrefix, "test"))
            .thenReturn(AdyenVerifyPaymentResponseModel(200, "no error", "message", "error type", "psp reference"))
    }

    @Test
    fun `calculate PSP config`() {
        adyenPsp.calculatePspConfig(pspConfig, dynamicPspConfig, true)
    }

    @Test
    fun `register alias`() {
        adyenPsp.registerAlias(PspRegisterAliasRequestModel(correctAliasId,
            AliasExtraModel(
                null,
                null,
                null,
                PersonalDataModel(
                    null,
                    null,
                    "lastName",
                    null,
                    null,
                    "Berlin",
                    country
                ),
                PaymentMethod.CC,
                correctPayload
            ), pspConfig), true)
    }
}
