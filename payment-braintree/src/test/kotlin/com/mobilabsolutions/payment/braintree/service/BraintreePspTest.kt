/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.service

import com.braintreegateway.Transaction
import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.exception.BraintreeErrors
import com.mobilabsolutions.payment.braintree.model.request.BraintreeCaptureRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreePaymentRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRefundRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeReverseRequestModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreePaymentResponseModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreeRegisterAliasResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.CreditCardConfigModel
import com.mobilabsolutions.payment.model.PayPalConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspDeleteAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.mobilabsolutions.server.commons.exception.ApiException
import com.mobilabsolutions.server.commons.exception.PaymentError
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
    private val wrongPspAlias = "wrong pspAlias"
    private val merchantId = "123"
    private val publicKey = "1234"
    private val privateKey = "12345"
    private val nonce = "test nonce"
    private val deviceData = "test device data"
    private val billingAgreementId = "billing agreement"
    private val test = true
    private val pspConfig = PspConfigModel(
        PaymentServiceProvider.BRAINTREE.toString(),
        null,
        null,
        null,
        null,
        merchantId,
        publicKey,
        privateKey,
        null,
        null,
        true,
        null,
        null,
        null,
        null,
        null
    )
    private val mode = "sandbox"
    private val correctAmount = 1000
    private val declinedAmount = 5000
    private val currency = "EUR"
    private val reason = "some reason"
    private val transactionId = "some transaction id"
    private val pspTransactionId = "some psp transaction id"
    private val wrongPspTransactionId = "some wrong psp transaction id"
    private val ccAliasId = "alias id"
    private val ccPspAlias = "psp alias 2"
    private val ccNonce = "cc nonce"
    private val ccPspTransactionId = "another transaction"
    private val ccTransactionId = "another transaction"

    @InjectMocks
    private lateinit var braintreePsp: BraintreePsp

    @Mock
    private lateinit var braintreeClient: BraintreeClient

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(braintreeClient.registerAlias(BraintreeRegisterAliasRequestModel(correctAliasId, nonce, deviceData),
            PspConfigModel(
                PaymentServiceProvider.BRAINTREE.toString(), null, null, null, null,
                merchantId, publicKey, privateKey, null, null, true, null, null, null, null, null
            ), BraintreeMode.SANDBOX.mode, PaymentMethod.PAY_PAL.name))
            .thenReturn(BraintreeRegisterAliasResponseModel(pspAlias, billingAgreementId))

        Mockito.`when`(braintreeClient.registerAlias(BraintreeRegisterAliasRequestModel(ccAliasId, ccNonce, deviceData),
            PspConfigModel(
                PaymentServiceProvider.BRAINTREE.toString(), null, null, null, null,
                merchantId, publicKey, privateKey, null, null, true, null, null, null, null, null
            ), BraintreeMode.SANDBOX.mode, PaymentMethod.CC.name))
            .thenReturn(BraintreeRegisterAliasResponseModel(ccPspAlias, null))

        Mockito.`when`(braintreeClient.preauthorization(BraintreePaymentRequestModel(correctAmount.toString(), pspAlias, deviceData), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.AUTHORIZED, transactionId = transactionId))
        Mockito.`when`(braintreeClient.preauthorization(BraintreePaymentRequestModel(declinedAmount.toString(), pspAlias, deviceData), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLEMENT_DECLINED, transactionId = transactionId, errorCode = BraintreeErrors.SETTLEMENT_DECLINED.code))
        Mockito.`when`(braintreeClient.preauthorization(BraintreePaymentRequestModel(correctAmount.toString(), ccPspAlias, deviceData), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.AUTHORIZED, transactionId = ccTransactionId))
        Mockito.`when`(braintreeClient.capture(BraintreeCaptureRequestModel(pspTransactionId), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLING, transactionId = transactionId))
        Mockito.`when`(braintreeClient.capture(BraintreeCaptureRequestModel(wrongPspTransactionId), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.FAILED, transactionId = wrongPspTransactionId, errorCode = PaymentError.PAYMENT_ERROR.name, errorMessage = BraintreeErrors.ALREADY_CAPTURED.name))
        Mockito.`when`(braintreeClient.capture(BraintreeCaptureRequestModel(ccPspTransactionId), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLING, transactionId = ccTransactionId))
        Mockito.`when`(braintreeClient.authorization(BraintreePaymentRequestModel(correctAmount.toString(), pspAlias, deviceData), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLING, transactionId = transactionId))
        Mockito.`when`(braintreeClient.authorization(BraintreePaymentRequestModel(declinedAmount.toString(), pspAlias, deviceData), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLEMENT_DECLINED, transactionId = transactionId, errorCode = BraintreeErrors.SETTLEMENT_DECLINED.code))
        Mockito.`when`(braintreeClient.authorization(BraintreePaymentRequestModel(correctAmount.toString(), ccPspAlias, deviceData), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLING, transactionId = ccTransactionId))
        Mockito.`when`(braintreeClient.refund(BraintreeRefundRequestModel(pspTransactionId, correctAmount.toString()), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLING, transactionId = transactionId))
        Mockito.`when`(braintreeClient.refund(BraintreeRefundRequestModel(ccPspTransactionId, correctAmount.toString()), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.SETTLING, transactionId = ccPspTransactionId))
        Mockito.`when`(braintreeClient.deleteAlias(wrongPspAlias, pspConfig, BraintreeMode.SANDBOX.mode))
            .thenThrow(ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "PayPal alias doesn't exist at Braintree").asException())
        Mockito.`when`(braintreeClient.reverse(BraintreeReverseRequestModel(pspTransactionId), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.VOIDED, transactionId = transactionId))
        Mockito.`when`(braintreeClient.reverse(BraintreeReverseRequestModel(wrongPspTransactionId), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.FAILED, transactionId = pspTransactionId, errorCode = PaymentError.PAYMENT_ERROR.name, errorMessage = BraintreeErrors.ALREADY_REVERSED.name))
        Mockito.`when`(braintreeClient.reverse(BraintreeReverseRequestModel(ccPspTransactionId), pspConfig, mode))
            .thenReturn(BraintreePaymentResponseModel(status = Transaction.Status.VOIDED, transactionId = ccTransactionId))
    }

    @Test
    fun `calculate PSP config`() {
        braintreePsp.calculatePspConfig(pspConfig, null, test)
    }

    @Test
    fun `register paypal alias with correct alias id`() {
        braintreePsp.registerAlias(PspRegisterAliasRequestModel(correctAliasId,
            AliasExtraModel(
                null,
                null,
                PayPalConfigModel(nonce, billingAgreementId, deviceData),
                null,
                null,
                PaymentMethod.PAY_PAL.name,
                null,
                null
            ), pspConfig), test)
    }

    @Test
    fun `register alias with wrong payment method`() {
        Assertions.assertThrows(ApiException::class.java) {
            braintreePsp.registerAlias(PspRegisterAliasRequestModel(correctAliasId,
                AliasExtraModel(
                    null,
                    null,
                    PayPalConfigModel(nonce, billingAgreementId, deviceData),
                    null,
                    null,
                    PaymentMethod.SEPA.name,
                    null,
                    null
                ), pspConfig), test)
        }
    }

        @Test
        fun `register cc alias with correct alias id`() {
            braintreePsp.registerAlias(PspRegisterAliasRequestModel(ccAliasId,
                AliasExtraModel(
                    CreditCardConfigModel(null, null, null, null, null, null, null, null, null, ccNonce, deviceData),
                    null,
                    null,
                    null,
                    null,
                    PaymentMethod.CC.name,
                    null,
                    null
                ), pspConfig), test)
        }

        @Test
        fun `delete paypal alias`() {
            braintreePsp.deleteAlias(
                PspDeleteAliasRequestModel(
                    null, pspAlias, null, pspConfig, null), test)
        }

        @Test
        fun `delete cc alias`() {
            braintreePsp.deleteAlias(
                PspDeleteAliasRequestModel(
                    null, ccPspAlias, null, pspConfig, null), test)
        }

        @Test
        fun `delete non existing alias`() {
            Assertions.assertThrows(ApiException::class.java) {
                braintreePsp.deleteAlias(
                    PspDeleteAliasRequestModel(
                        null, wrongPspAlias, null, pspConfig, null), test)
            }
        }

        @Test
        fun `preauthorize paypal transaction successfully`() {
            val response = braintreePsp.preauthorize(
                PspPaymentRequestModel(
                    null,
                    AliasExtraModel(
                        null,
                        null,
                        PayPalConfigModel(nonce, billingAgreementId, deviceData),
                        null,
                        null,
                        PaymentMethod.PAY_PAL.name,
                        null,
                        null
                    ),
                    PaymentDataRequestModel(correctAmount, currency, reason),
                    pspAlias,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `preauthorize with declined amount`() {
            val response = braintreePsp.preauthorize(
                PspPaymentRequestModel(
                    null,
                    AliasExtraModel(
                        null,
                        null,
                        PayPalConfigModel(nonce, billingAgreementId, deviceData),
                        null,
                        null,
                        PaymentMethod.PAY_PAL.name,
                        null,
                        null
                    ),
                    PaymentDataRequestModel(declinedAmount, currency, reason),
                    pspAlias,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertEquals(response.error, PaymentError.PAYMENT_ERROR)
        }

        @Test
        fun `preauthorize cc transaction successfully`() {
            val response = braintreePsp.preauthorize(
                PspPaymentRequestModel(
                    null,
                    AliasExtraModel(
                        CreditCardConfigModel(null, null, null, null, null, null, null, null, null, ccNonce, deviceData),
                        null,
                        null,
                        null,
                        null,
                        PaymentMethod.CC.name,
                        null,
                        null
                    ),
                    PaymentDataRequestModel(correctAmount, currency, reason),
                    ccPspAlias,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `capture paypal transaction successfully`() {
            val response = braintreePsp.capture(
                PspCaptureRequestModel(
                    pspTransactionId,
                    null,
                    null,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `capture unsuccessfully`() {
            val response = braintreePsp.capture(
                PspCaptureRequestModel(
                    wrongPspTransactionId,
                    null,
                    null,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertEquals(response.status, TransactionStatus.FAIL)
            Assertions.assertNotNull(response.error)
        }

        @Test
        fun `capture cc transaction successfully`() {
            val response = braintreePsp.capture(
                PspCaptureRequestModel(
                    ccPspTransactionId,
                    null,
                    null,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `authorize paypal transaction successfully`() {
            val response = braintreePsp.authorize(
                PspPaymentRequestModel(
                    null,
                    AliasExtraModel(
                        null,
                        null,
                        PayPalConfigModel(nonce, billingAgreementId, deviceData),
                        null,
                        null,
                        PaymentMethod.PAY_PAL.name,
                        null,
                        null
                    ),
                    PaymentDataRequestModel(correctAmount, currency, reason),
                    pspAlias,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `authorize with declined amount`() {
            val response = braintreePsp.authorize(
                PspPaymentRequestModel(
                    null,
                    AliasExtraModel(
                        null,
                        null,
                        PayPalConfigModel(nonce, billingAgreementId, deviceData),
                        null,
                        null,
                        PaymentMethod.PAY_PAL.name,
                        null,
                        null
                    ),
                    PaymentDataRequestModel(declinedAmount, currency, reason),
                    pspAlias,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertEquals(response.error, PaymentError.PAYMENT_ERROR)
        }

        @Test
        fun `authorize cc transaction successfully`() {
            val response = braintreePsp.authorize(
                PspPaymentRequestModel(
                    null,
                    AliasExtraModel(
                        CreditCardConfigModel(null, null, null, null, null, null, null, null, null, ccNonce, deviceData),
                        null,
                        null,
                        null,
                        null,
                        PaymentMethod.CC.name,
                        null,
                        null
                    ),
                    PaymentDataRequestModel(correctAmount, currency, reason),
                    ccPspAlias,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `refund paypal transaction successfully`() {
            val response = braintreePsp.refund(
                PspRefundRequestModel(
                    pspTransactionId,
                    correctAmount,
                    currency,
                    TransactionAction.REFUND.name,
                    pspConfig,
                    null,
                    PaymentMethod.PAY_PAL.name
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `refund cc transaction successfully`() {
            val response = braintreePsp.refund(
                PspRefundRequestModel(
                    ccPspTransactionId,
                    correctAmount,
                    currency,
                    TransactionAction.REFUND.name,
                    pspConfig,
                    null,
                    PaymentMethod.CC.name
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `reverse paypal transaction successfully`() {
            val response = braintreePsp.reverse(
                PspReversalRequestModel(
                    pspTransactionId,
                    currency,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `reverse cc transaction successfully`() {
            val response = braintreePsp.reverse(
                PspReversalRequestModel(
                    ccPspTransactionId,
                    currency,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertNull(response.error)
        }

        @Test
        fun `reverse unsuccessfully`() {
            val response = braintreePsp.reverse(
                PspReversalRequestModel(
                    wrongPspTransactionId,
                    currency,
                    pspConfig,
                    null
                ), test
            )
            Assertions.assertEquals(response.status, TransactionStatus.FAIL)
            Assertions.assertNotNull(response.error)
        }
}
