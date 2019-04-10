package com.mobilabsolutions.payment.braintree.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.model.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.braintree.model.BraintreeRegisterAliasResponseModel
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PayPalConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.server.commons.CommonConfiguration
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BraintreePspTest {
    private val correctAliasId = "correct id"
    private val wrongAliasId = "wrong id"
    private val pspAlias = "token 1"
    private val merchantId = "123"
    private val publicKey = "1234"
    private val privateKey = "12345"
    private val nonce = "test nonce"
    private val deviceData = "test device data"
    private val billingAgreementId = "billing agreement"
    private val test = true
    private val merchantConfig = "{\"psp\" : [{\"type\" : \"BRAINTREE\", \"sandboxMerchantId\" : \"123\"," +
        " \"sandboxPublicKey\" : \"1234\", \"sandboxPrivateKey\" : \"12345\"}]}"
    private val extraPayPal =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"PAY_PAL\", \"payPalConfig\": {\"nonce\": \"test nonce\",\"deviceData\": \"test device data\"}}"

    @InjectMocks
    private lateinit var braintreePsp: BraintreePsp

    @Mock
    private lateinit var aliasIdRepository: AliasRepository

    @Mock
    private lateinit var braintreeClient: BraintreeClient

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(correctAliasId, true)).thenReturn(
            Alias(id = correctAliasId, active = true, extra = extraPayPal, psp = PaymentServiceProvider.BRAINTREE, pspAlias = pspAlias,
                merchant = Merchant(id = "1", pspConfig = merchantConfig))
        )

        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(wrongAliasId, true)).thenReturn(null)
        Mockito.`when`(braintreeClient.registerPayPal(BraintreeRegisterAliasRequestModel(correctAliasId, nonce, deviceData),
            PspConfigModel(PaymentServiceProvider.BRAINTREE.toString(), null, null, null, null,
                merchantId, publicKey, privateKey, null, null, true), BraintreeMode.SANDBOX.mode))
            .thenReturn(BraintreeRegisterAliasResponseModel(pspAlias, billingAgreementId))
    }

    @Test
    fun `register alias with correct alias id`() {
        braintreePsp.registerAlias(correctAliasId, AliasExtraModel(null, null, PayPalConfigModel(nonce, billingAgreementId, deviceData), null, PaymentMethod.PAY_PAL), test)
    }

    @Test
    fun `register alias with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            braintreePsp.registerAlias(wrongAliasId, AliasExtraModel(null, null, PayPalConfigModel(nonce, billingAgreementId, deviceData), null, PaymentMethod.PAY_PAL), test)
        }
    }

    @Test
    fun `register alias with wrong payment method`() {
        Assertions.assertThrows(ApiException::class.java) {
            braintreePsp.registerAlias(correctAliasId, AliasExtraModel(null, null, PayPalConfigModel(nonce, billingAgreementId, deviceData), null, PaymentMethod.CC), test)
        }
    }

    @Test
    fun `calculate PSP config`() {
        braintreePsp.calculatePspConfig(PspConfigModel(PaymentServiceProvider.BRAINTREE.toString(), null, null, null, null, merchantId, publicKey, privateKey, null, null, true), test)
    }
}
