package com.mobilabsolutions.payment.braintree.service

import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreeRegisterAliasResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.request.AliasExtraModel
import com.mobilabsolutions.payment.model.request.PayPalConfigModel
import com.mobilabsolutions.payment.model.request.PspConfigModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
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
    private val pspAlias = "pspAlias 1"
    private val merchantId = "123"
    private val publicKey = "1234"
    private val privateKey = "12345"
    private val nonce = "test nonce"
    private val deviceData = "test device data"
    private val billingAgreementId = "billing agreement"
    private val test = true
    private val pspConfig = PspConfigModel(
        PaymentServiceProvider.BRAINTREE.toString(), null, null, null, null, merchantId, publicKey, privateKey, null, null, true)

    @InjectMocks
    private lateinit var braintreePsp: BraintreePsp

    @Mock
    private lateinit var braintreeClient: BraintreeClient

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(braintreeClient.registerPayPal(BraintreeRegisterAliasRequestModel(correctAliasId, nonce, deviceData),
            PspConfigModel(PaymentServiceProvider.BRAINTREE.toString(), null, null, null, null,
                merchantId, publicKey, privateKey, null, null, true), BraintreeMode.SANDBOX.mode))
            .thenReturn(BraintreeRegisterAliasResponseModel(pspAlias, billingAgreementId))
    }

    @Test
    fun `register alias with correct alias id`() {
        braintreePsp.registerAlias(PspRegisterAliasRequestModel(correctAliasId, AliasExtraModel(null, null, PayPalConfigModel(nonce, billingAgreementId, deviceData), null, PaymentMethod.PAY_PAL), pspConfig), test)
    }

    @Test
    fun `register alias with wrong payment method`() {
        Assertions.assertThrows(ApiException::class.java) {
            braintreePsp.registerAlias(PspRegisterAliasRequestModel(correctAliasId, AliasExtraModel(null, null, PayPalConfigModel(nonce, billingAgreementId, deviceData), null, PaymentMethod.CC), pspConfig), test)
        }
    }

    @Test
    fun `calculate PSP config`() {
        braintreePsp.calculatePspConfig(pspConfig, test)
    }
}
