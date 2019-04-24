package com.mobilabsolutions.payment.bspayone.service

import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneClearingType
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneMode
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneResponseStatus
import com.mobilabsolutions.payment.bspayone.exception.BsPayoneErrors
import com.mobilabsolutions.payment.bspayone.model.request.BsPayoneCaptureRequestModel
import com.mobilabsolutions.payment.bspayone.model.request.BsPayoneDeleteAliasRequestModel
import com.mobilabsolutions.payment.bspayone.model.request.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.bspayone.model.request.BsPayoneRefundRequestModel
import com.mobilabsolutions.payment.bspayone.model.response.BsPayoneDeleteAliasResponseModel
import com.mobilabsolutions.payment.bspayone.model.response.BsPayonePaymentResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.SepaConfigModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspDeleteAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
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
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BsPayonePspTest {
    private val correctCcAliasId = "cc test"
    private val correctSepaAliasId = "sepa test"
    private val reference = "1234567890"
    private val amount = 300
    private val wrongAmount = -1
    private val currency = "EUR"
    private val customerId = "1"
    private val pspTransactionId = "1123"
    private val wrongPspTransactionId = "1123"
    private val accountId = "123"
    private val portalId = "123"
    private val key = "123"
    private val merchantId = "mobilab"
    private val lastName = "Mustermann"
    private val country = "DE"
    private val city = "Berlin"
    private val pspAlias = "1234"
    private val reason = "Book"
    private val iban = "DE00123456782599100004"
    private val bic = "TESTTEST"
    private val test = true
    private val reversalAmount = 0
    private val captureSequenceNumber = 2
    private val pspConfig = PspConfigModel(
        PaymentServiceProvider.BS_PAYONE.toString(),
        merchantId,
        portalId,
        key,
        accountId,
        null,
        null,
        null,
        null,
        null,
        true,
        null
    )

    @InjectMocks
    private lateinit var bsPayonePsp: BsPayonePsp

    @Mock
    private lateinit var bsPayoneHashingService: BsPayoneHashingService

    @Mock
    private lateinit var bsPayoneClient: BsPayoneClient

    @Mock
    private lateinit var bsPayoneProperties: BsPayoneProperties

    @Mock
    private lateinit var randomStringGenerator: RandomStringGenerator

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(randomStringGenerator.generateRandomAlphanumeric(10)).thenReturn(reference)
        Mockito.`when`(bsPayoneClient.preauthorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.CC.type,
            reference, amount.toString(), currency, correctCcAliasId, lastName, country, city, pspAlias, null, null), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.preauthorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.CC.type,
            reference, wrongAmount.toString(), currency, correctCcAliasId, lastName, country, city, pspAlias, null, null), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.ERROR, null, null, BsPayoneErrors.AMOUNT_TOO_LOW.code, BsPayoneErrors.AMOUNT_TOO_LOW.error.error, "Please change the amount")
            )
        Mockito.`when`(bsPayoneClient.authorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.SEPA.type,
            reference, amount.toString(), currency, correctSepaAliasId, lastName, country, city, null, iban, bic), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.authorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.SEPA.type,
            reference, wrongAmount.toString(), currency, correctSepaAliasId, lastName, country, city, null, iban, bic), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.ERROR, null, null, BsPayoneErrors.AMOUNT_TOO_LOW.code, BsPayoneErrors.AMOUNT_TOO_LOW.error.error, "Please change the amount")
            )
        Mockito.`when`(bsPayoneClient.capture(BsPayoneCaptureRequestModel(pspTransactionId, amount.toString(), currency), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.capture(BsPayoneCaptureRequestModel(pspTransactionId, wrongAmount.toString(), currency), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.ERROR, null, null, BsPayoneErrors.AMOUNT_TOO_LOW.code, BsPayoneErrors.AMOUNT_TOO_LOW.error.error, "Please change the amount")
            )
        Mockito.`when`(bsPayoneClient.capture(BsPayoneCaptureRequestModel(wrongPspTransactionId, amount.toString(), currency), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.ERROR, null, null, BsPayoneErrors.TRANSACTION_NOT_FOUND.code, BsPayoneErrors.TRANSACTION_NOT_FOUND.error.error, "Transaction id wrong or missing")
            )
        Mockito.`when`(bsPayoneClient.deleteAlias(BsPayoneDeleteAliasRequestModel(correctCcAliasId, "yes", "no"), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(BsPayoneDeleteAliasResponseModel(BsPayoneResponseStatus.OK, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.capture(BsPayoneCaptureRequestModel(pspTransactionId, reversalAmount.toString(), currency), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.refund(BsPayoneRefundRequestModel(pspTransactionId, captureSequenceNumber, (amount * -1).toString(), currency), pspConfig, BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
    }

    @Test
    fun `preauthorize transaction with correct alias id`() {
        bsPayonePsp.preauthorize(PspPaymentRequestModel(correctCcAliasId,
            AliasExtraModel(null, null, null,
                PersonalDataModel(null, null, lastName, null, null, city, country), PaymentMethod.CC),
            PaymentDataRequestModel(amount, currency, reason), pspAlias, pspConfig), test)
    }

    @Test
    fun `authorize transaction with correct alias id`() {
        bsPayonePsp.authorize(PspPaymentRequestModel(correctSepaAliasId,
            AliasExtraModel(null, SepaConfigModel(iban, bic), null, PersonalDataModel(null, null, lastName, null, null, city, country), PaymentMethod.SEPA),
            PaymentDataRequestModel(amount, currency, reason), null, pspConfig), test)
    }

    @Test
    fun `preauthorize with wrong payment method`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.preauthorize(PspPaymentRequestModel(correctCcAliasId, AliasExtraModel(null, null, null, PersonalDataModel(null, null, lastName, null, null, city, country), PaymentMethod.PAY_PAL), PaymentDataRequestModel(amount, currency, reason), pspAlias, pspConfig), test)
        }
    }

    @Test
    fun `authorize with wrong payment method`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.authorize(PspPaymentRequestModel(correctCcAliasId, AliasExtraModel(null, null, null, PersonalDataModel(null, null, lastName, null, null, city, country), PaymentMethod.PAY_PAL), PaymentDataRequestModel(amount, currency, reason), pspAlias, pspConfig), test)
        }
    }

    @Test
    fun `capture transaction with correct transaction id`() {
        bsPayonePsp.capture(PspCaptureRequestModel(pspTransactionId, amount, currency, pspConfig), test)
    }

    @Test
    fun `reverse transaction with correct transaction id`() {
        bsPayonePsp.reverse(PspReversalRequestModel(pspTransactionId, currency, pspConfig), test)
    }

    @Test
    fun `refund transaction with correct transaction id`() {
        bsPayonePsp.refund(PspRefundRequestModel(pspTransactionId, amount, currency, TransactionAction.CAPTURE, pspConfig), test)
    }

    @Test
    fun `calculate PSP config`() {
        bsPayonePsp.calculatePspConfig(pspConfig, true)
    }

    @Test
    fun `delete alias`() {
        bsPayonePsp.deleteAlias(PspDeleteAliasRequestModel(correctCcAliasId, null, PaymentMethod.CC, pspConfig), test)
    }
}
