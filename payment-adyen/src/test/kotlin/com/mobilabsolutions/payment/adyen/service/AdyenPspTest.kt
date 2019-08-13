/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.adyen.configuration.AdyenProperties
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.model.request.AdyenAdditionalDataModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenAmountRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenCaptureRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenDeleteAliasRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentMethodRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRecurringRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRefundRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenReverseRequestModel
import com.mobilabsolutions.payment.adyen.model.response.Adyen3DSecureResponseModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenPaymentResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.CreditCardConfigModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.SepaConfigModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspDeleteAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
import com.mobilabsolutions.server.commons.CommonConfiguration
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.mobilabsolutions.server.commons.exception.ApiException
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
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
    private val amountValue = 300
    private val clientToken = "testtoken"
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
        urlPrefix,
        clientToken
    )
    private val email = "test@test.com"
    private val customerIP = "61.294.12.12"
    private val aliasId = "alias id"
    private val reference = "reference"
    private val pspReference = "1234567890"
    private val pspAlias = "sje324andls"
    private val correctAliasId = "correct id"
    private val customerReference = "oIXHpTAfEPSleWXT6Khe"
    private val deletedCustomerReference = "dddddssss"
    private val holderName = "Max Mustermann"
    private val iban = "DE87123456781234567890"
    private val pspTransactionId = "12345"
    private val purchaseId = "37293728"
    private val encryptedCCNumber = "test credit card number"
    private val encryptedExpiryMonth = "test expiry month"
    private val encryptedExpiryYear = "test expiry year"
    private val encryptedSecurityCode = "test security code"
    private val channel = "iOS"
    private val returnUrl = "www.mblb.net"
    private val paymentData = "test payment data"

    @InjectMocks
    private lateinit var adyenPsp: AdyenPsp

    @Mock
    private lateinit var adyenClient: AdyenClient

    @Mock
    private lateinit var adyenProperties: AdyenProperties

    @Mock
    private lateinit var randomStringGenerator: RandomStringGenerator

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(randomStringGenerator.generateRandomAlphanumeric(20)).thenReturn(reference)
        Mockito.`when`(adyenClient.preauthorization(
            AdyenPaymentRequestModel(amount, email, customerIP, customerReference, pspAlias,
                AdyenRecurringRequestModel(adyenProperties.contract), adyenProperties.shopperInteraction, reference, sandboxMerchantId, null, null, null, null, null, null),
            pspConfig, AdyenMode.TEST.mode))
            .thenReturn(AdyenPaymentResponseModel(pspReference, null, null))
        Mockito.`when`(randomStringGenerator.generateRandomAlphanumeric(20)).thenReturn(reference)
        Mockito.`when`(adyenClient.authorization(
            AdyenPaymentRequestModel(amount, email, customerIP, customerReference, pspAlias,
                AdyenRecurringRequestModel(adyenProperties.contract), adyenProperties.shopperInteraction, reference, sandboxMerchantId, 0, null, null, null, null, null),
            pspConfig, AdyenMode.TEST.mode))
            .thenReturn(AdyenPaymentResponseModel(pspReference, null, null))
        Mockito.`when`(adyenClient.sepaPayment(
            AdyenPaymentRequestModel(amount, null, null, null, null,
                null, null, reference, sandboxMerchantId, null,
                AdyenPaymentMethodRequestModel(adyenProperties.sepaPaymentMethod, holderName, iban, null, null, null, null, null), null, null, null, null),
            pspConfig, AdyenMode.TEST.mode))
            .thenReturn(AdyenPaymentResponseModel(pspReference, null, null))
        Mockito.`when`(adyenClient.reverse(AdyenReverseRequestModel(pspTransactionId, purchaseId, sandboxMerchantId), pspConfig, "test"))
            .thenReturn(AdyenPaymentResponseModel(pspReference, null, null))
        Mockito.`when`(adyenClient.capture(AdyenCaptureRequestModel(pspTransactionId, amount, purchaseId, sandboxMerchantId), pspConfig, "test"))
            .thenReturn(AdyenPaymentResponseModel(pspReference, null, null))
        Mockito.`when`(adyenClient.refund(AdyenRefundRequestModel(pspReference, AdyenAmountRequestModel(amountValue, currency), reference, sandboxMerchantId), pspConfig, AdyenMode.TEST.mode))
            .thenReturn(AdyenPaymentResponseModel(pspReference, null, null))
        Mockito.`when`(adyenClient.sepaRefund(AdyenRefundRequestModel(pspReference, null, reference, sandboxMerchantId), pspConfig, AdyenMode.TEST.mode))
            .thenReturn(AdyenPaymentResponseModel(pspReference, null, null))
        Mockito.`when`(adyenClient.deleteAlias(AdyenDeleteAliasRequestModel(deletedCustomerReference, pspAlias, sandboxMerchantId), pspConfig, "true"))
            .thenThrow(ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Alias doesn't exist at Adyen").asException())
        Mockito.`when`(adyenClient.registerThreeDSecure(AdyenPaymentRequestModel(AdyenAmountRequestModel(0, currency), email, customerIP, correctAliasId, null, null, null, reference, sandboxMerchantId, null,
                AdyenPaymentMethodRequestModel(adyenProperties.threeDSecure, null, null, encryptedCCNumber, encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, true), AdyenAdditionalDataModel(true), channel, returnUrl, true), pspConfig, AdyenMode.TEST.mode))
            .thenReturn(Adyen3DSecureResponseModel(paymentData, "IdentifyShopper", "fingerprint", null, "fingerprint3DS2", "scheme", null, null))
    }

    @Test
    fun `calculate PSP config`() {
        adyenPsp.calculatePspConfig(pspConfig, true)
    }

    @Test
    fun `authorize credit card successfully`() {
        adyenPsp.authorize(PspPaymentRequestModel(
            aliasId,
            AliasExtraModel(
                null,
                null,
                null,
                null,
                PersonalDataModel(
                    email,
                    customerIP,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    customerReference),
                PaymentMethod.CC.name, null),
            PaymentDataRequestModel(amountValue, currency, "Book"),
            pspAlias, pspConfig, null), true)
    }

    @Test
    fun `make sepa payment successfully`() {
        adyenPsp.authorize(PspPaymentRequestModel(
            aliasId,
            AliasExtraModel(
                null,
                SepaConfigModel(iban, null),
                null,
                null,
                PersonalDataModel(
                    null,
                    null,
                    "Max",
                    "Mustermann",
                    null,
                    null,
                    null,
                    null,
                    null),
                PaymentMethod.SEPA.name, null),
            PaymentDataRequestModel(amountValue, currency, "Book"),
            null, pspConfig, null), true)
    }

    @Test
    fun `authorization with wrong payment method`() {
        Assertions.assertThrows(ApiException::class.java) {
            adyenPsp.authorize(PspPaymentRequestModel(
                aliasId,
                AliasExtraModel(
                    null,
                    null,
                    null,
                    null,
                    PersonalDataModel(
                        email,
                        customerIP,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        customerReference),
                    PaymentMethod.PAY_PAL.name, null),
                PaymentDataRequestModel(amountValue, currency, "Book"),
                pspAlias, pspConfig, null), true)
        }
    }

    @Test
    fun `register alias`() {
        adyenPsp.registerAlias(PspRegisterAliasRequestModel(correctAliasId,
            AliasExtraModel(
                CreditCardConfigModel(
                    null,
                    null,
                    null,
                    null,
                    encryptedCCNumber,
                    encryptedExpiryMonth,
                    encryptedExpiryYear,
                    encryptedSecurityCode,
                    returnUrl,
                    null,
                    null
                ),
                null,
                null,
                null,
                PersonalDataModel(
                    email,
                    customerIP,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    customerReference
                ),
                PaymentMethod.CC.name, channel), pspConfig), true)
    }

    @Test
    fun `capture successfully`() {
        adyenPsp.capture(PspCaptureRequestModel(
            pspTransactionId,
            amountValue,
            currency,
            pspConfig,
            purchaseId
        ), true)
    }

    @Test
    fun `preauthorize successfully`() {
        adyenPsp.preauthorize(PspPaymentRequestModel(
            aliasId,
            AliasExtraModel(
                null,
                null,
                null,
                null,
                PersonalDataModel(
                    email,
                    customerIP,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    customerReference),
                PaymentMethod.CC.name, null),
            PaymentDataRequestModel(amountValue, currency, "Book"),
            pspAlias, pspConfig, null), true)
    }

    @Test
    fun `refund cc payment successfully`() {
        adyenPsp.refund(
            PspRefundRequestModel(
                pspReference,
                amountValue,
                currency,
                TransactionAction.AUTH.name,
                pspConfig,
                null,
                PaymentMethod.CC.name
            ), true
        )
    }

    @Test
    fun `refund sepa payment successfully`() {
        adyenPsp.refund(
            PspRefundRequestModel(
                pspReference,
                null,
                null,
                TransactionAction.AUTH.name,
                pspConfig,
                null,
                PaymentMethod.SEPA.name
            ), true
        )
    }

    @Test
    fun `refund with wrong payment method`() {
        Assertions.assertThrows(ApiException::class.java) {
            adyenPsp.refund(
                PspRefundRequestModel(
                    pspReference,
                    amountValue,
                    currency,
                    TransactionAction.AUTH.name,
                    pspConfig,
                    null,
                    PaymentMethod.PAY_PAL.name
                ), true
            )
        }
    }

    @Test
    fun `reverse successfully`() {
        adyenPsp.reverse(PspReversalRequestModel(
            pspTransactionId,
            currency,
            pspConfig,
            purchaseId
        ), true)
    }

    @Test
    fun `delete alias successfully`() {
        adyenPsp.deleteAlias(
            PspDeleteAliasRequestModel(
                aliasId,
                pspAlias,
                PaymentMethod.CC.name,
                pspConfig,
                customerReference
            ), true
        )
    }

    @Test
    fun `delete non existing alias`() {
        adyenPsp.deleteAlias(
            PspDeleteAliasRequestModel(
                aliasId,
                pspAlias,
                PaymentMethod.CC.name,
                pspConfig,
                customerReference
            ), true
        )
    }
}
