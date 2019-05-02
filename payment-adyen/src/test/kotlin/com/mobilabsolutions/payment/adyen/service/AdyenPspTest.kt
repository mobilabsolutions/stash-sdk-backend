package com.mobilabsolutions.payment.adyen.service

import com.mobilabsolutions.payment.adyen.configuration.AdyenProperties
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.data.enum.AdyenResultCode
import com.mobilabsolutions.payment.adyen.model.request.AdyenAmountRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRecurringRequestModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenPaymentResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
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
    private val dynamicPspConfig = DynamicPspConfigRequestModel(
        "some token",
        "some url",
        "some channel"
    )
    private val amountValue = 300
    private val username = "username"
    private val password = "password"
    private val amount = AdyenAmountRequestModel(
        value = amountValue,
        currency = currency
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
        null,
        username,
        null,
        password,
        null
    )
    private val paymentSession = "123"
    private val email = "test@test.com"
    private val customerIP = "61.294.12.12"
    private val aliasId = "alias id"
    private val reference = "reference"
    private val pspReference = "1234567890"
    private val pspAlias = "psp alias"

    @InjectMocks
    private lateinit var adyenPsp: AdyenPsp

    @Mock
    private lateinit var adyenClient: AdyenClient

    @Mock
    private lateinit var adyenProperties: AdyenProperties

    @Mock
    private lateinit var randomStringGenerator: RandomStringGenerator

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(adyenClient.requestPaymentSession(pspConfig, dynamicPspConfig, "test"))
            .thenReturn(paymentSession)
        Mockito.`when`(randomStringGenerator.generateRandomAlphanumeric(20)).thenReturn(reference)
        Mockito.`when`(adyenClient.authorization(
            AdyenPaymentRequestModel(amount, email, customerIP, aliasId, adyenProperties.selectedRecurringDetailReference,
                AdyenRecurringRequestModel(adyenProperties.contract), adyenProperties.shopperInteraction, reference, sandboxMerchantId, 0), pspConfig, AdyenMode.TEST.mode))
            .thenReturn(AdyenPaymentResponseModel(pspReference, AdyenResultCode.AUTHORISED.result, null))
    }

    @Test
    fun `calculate PSP config`() {
        adyenPsp.calculatePspConfig(pspConfig, dynamicPspConfig, true)
    }

    @Test
    fun `authorize successfully`() {
        adyenPsp.authorize(PspPaymentRequestModel(
            aliasId, AliasExtraModel(null, null, null, PersonalDataModel(email, customerIP, null, null, null, null, null, null), PaymentMethod.CC),
            PaymentDataRequestModel(amountValue, currency, "Book"), pspAlias, pspConfig), true)
    }
}
