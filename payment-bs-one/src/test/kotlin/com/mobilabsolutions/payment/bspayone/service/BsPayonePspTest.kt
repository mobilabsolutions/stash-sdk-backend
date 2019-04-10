package com.mobilabsolutions.payment.bspayone.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneClearingType
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneMode
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneResponseStatus
import com.mobilabsolutions.payment.bspayone.exception.BsPayoneErrors
import com.mobilabsolutions.payment.bspayone.model.BsPayoneCaptureRequestModel
import com.mobilabsolutions.payment.bspayone.model.BsPayoneDeleteAliasModel
import com.mobilabsolutions.payment.bspayone.model.BsPayoneDeleteAliasResponseModel
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentResponseModel
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.PaymentDataModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.server.commons.CommonConfiguration
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
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BsPayonePspTest {
    private val correctCcAliasId = "cc test"
    private val wrongCcAliasId = "cc wrong id"
    private val correctSepaAliasId = "sepa test"
    private val wrongSepaAliasId = "sepa wrong id"
    private val merchantConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val extraCC =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\", \"personalData\": {\"lastName\": \"Mustermann\",\"city\": \"Berlin\", \"country\": \"DE\"}}"
    private val extraSEPA =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"SEPA\", \"personalData\": {\"lastName\": \"Mustermann\",\"city\": \"Berlin\", \"country\": \"DE\"}, \"sepaConfig\": {\"iban\": \"DE00123456782599100004\",\"bic\": \"TESTTEST\"}}"
    private val reference = "1234567890"
    private val amount = 300
    private val wrongAmount = -1
    private val currency = "EUR"
    private val customerId = "1"
    private val purchaseId = "1"
    private val correctTransactionId = "99"
    private val wrongTransactionId = "-1"
    private val pspTransactionId = "1123"
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
    private val pspResponse = "{\"pspTransactionId\":\"325105132\",\"status\":\"SUCCESS\",\"customerId\":\"160624370\"}"
    private val test = true
    private val reversalAmount = 0

    @InjectMocks
    private lateinit var bsPayonePsp: BsPayonePsp

    @Mock
    private lateinit var bsPayoneHashingService: BsPayoneHashingService

    @Mock
    private lateinit var bsPayoneClient: BsPayoneClient

    @Mock
    private lateinit var aliasIdRepository: AliasRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var bsPayoneProperties: BsPayoneProperties

    @Mock
    private lateinit var randomStringGenerator: RandomStringGenerator

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(correctCcAliasId, true)).thenReturn(
            Alias(id = correctCcAliasId, active = true, extra = extraCC, psp = PaymentServiceProvider.BS_PAYONE, pspAlias = pspAlias,
                merchant = Merchant(id = "1", pspConfig = merchantConfig))
        )

        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(correctSepaAliasId, true)).thenReturn(
            Alias(id = correctSepaAliasId, active = true, extra = extraSEPA, psp = PaymentServiceProvider.BS_PAYONE, pspAlias = pspAlias,
                merchant = Merchant(id = "1", pspConfig = merchantConfig))
        )
        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(wrongCcAliasId, true)).thenReturn(null)
        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(wrongSepaAliasId, true)).thenReturn(null)
        Mockito.`when`(randomStringGenerator.generateRandomAlphanumeric(10)).thenReturn(reference)
        Mockito.`when`(transactionRepository.getByTransactionIdAndAction(correctTransactionId, TransactionAction.PREAUTH, TransactionStatus.SUCCESS))
            .thenReturn(Transaction(
                amount = amount,
                currencyId = currency,
                pspTestMode = test,
                merchant = Merchant("1", pspConfig = merchantConfig),
                alias = Alias(active = true, extra = extraCC, psp = PaymentServiceProvider.BS_PAYONE, merchant = Merchant("1", pspConfig = merchantConfig)),
                pspResponse = pspResponse
            ))
        Mockito.`when`(bsPayoneClient.preauthorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.CC.type,
            reference, amount.toString(), currency, correctCcAliasId, lastName, country, city, pspAlias, null, null),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.preauthorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.CC.type,
            reference, wrongAmount.toString(), currency, correctCcAliasId, lastName, country, city, pspAlias, null, null),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.ERROR, null, null, BsPayoneErrors.AMOUNT_TOO_LOW.code, BsPayoneErrors.AMOUNT_TOO_LOW.error.error, "Please change the amount")
            )
        Mockito.`when`(bsPayoneClient.authorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.SEPA.type,
            reference, amount.toString(), currency, correctSepaAliasId, lastName, country, city, pspAlias, iban, bic),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.authorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.SEPA.type,
            reference, wrongAmount.toString(), currency, correctSepaAliasId, lastName, country, city, pspAlias, iban, bic),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.ERROR, null, null, BsPayoneErrors.AMOUNT_TOO_LOW.code, BsPayoneErrors.AMOUNT_TOO_LOW.error.error, "Please change the amount")
            )
        Mockito.`when`(bsPayoneClient.capture(BsPayoneCaptureRequestModel(pspTransactionId, amount.toString(), currency),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.capture(BsPayoneCaptureRequestModel(pspTransactionId, amount.toString(), currency),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.ERROR, null, null, BsPayoneErrors.AMOUNT_TOO_LOW.code, BsPayoneErrors.AMOUNT_TOO_LOW.error.error, "Please change the amount")
            )
        Mockito.`when`(bsPayoneClient.deleteAlias(BsPayoneDeleteAliasModel(correctCcAliasId, "yes", "no"),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(BsPayoneDeleteAliasResponseModel(BsPayoneResponseStatus.OK, null, null, null)
            )
        Mockito.`when`(bsPayoneClient.capture(BsPayoneCaptureRequestModel(pspTransactionId, reversalAmount.toString(), currency),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), BsPayoneMode.TEST.mode))
            .thenReturn(
                BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
            )
    }

    @Test
    fun `preauthorize transaction with correct alias id`() {
        bsPayonePsp.preauthorize(PaymentRequestModel(correctCcAliasId, PaymentDataModel(amount, currency, reason), purchaseId, customerId), test)
    }

    @Test
    fun `preauthorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.preauthorize(PaymentRequestModel(wrongCcAliasId, PaymentDataModel(amount, currency, reason), purchaseId, customerId), test)
        }
    }

    @Test
    fun `preauthorize transaction with wrong amount`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.preauthorize(PaymentRequestModel(wrongCcAliasId, PaymentDataModel(wrongAmount, currency, reason), purchaseId, customerId), test)
        }
    }

    @Test
    fun `preauthorize test transaction with no mode`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.preauthorize(PaymentRequestModel(wrongCcAliasId, PaymentDataModel(wrongAmount, currency, reason), purchaseId, customerId), null)
        }
    }

    @Test
    fun `authorize transaction with correct alias id`() {
        bsPayonePsp.authorize(PaymentRequestModel(correctSepaAliasId, PaymentDataModel(amount, currency, reason), purchaseId, customerId), test)
    }

    @Test
    fun `authorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.authorize(PaymentRequestModel(wrongSepaAliasId, PaymentDataModel(amount, currency, reason), purchaseId, customerId), test)
        }
    }

    @Test
    fun `authorize transaction with wrong amount`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.authorize(PaymentRequestModel(wrongSepaAliasId, PaymentDataModel(wrongAmount, currency, reason), purchaseId, customerId), test)
        }
    }

    @Test
    fun `authorize test transaction with no mode`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.authorize(PaymentRequestModel(wrongSepaAliasId, PaymentDataModel(wrongAmount, currency, reason), purchaseId, customerId), null)
        }
    }

    @Test
    fun `capture transaction with correct transaction id`() {
        bsPayonePsp.capture(correctTransactionId, pspTransactionId, test)
    }

    @Test
    fun `capture transaction with wrong transaction id`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.capture(wrongTransactionId, pspTransactionId, test)
        }
    }

    @Test
    fun `capture test transaction with no mode`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.capture(wrongTransactionId, pspTransactionId, null)
        }
    }

    @Test
    fun `reverse transaction with correct transaction id`() {
        bsPayonePsp.reverse(correctTransactionId, pspTransactionId, test)
    }

    @Test
    fun `reverse transaction with wrong transaction id`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.reverse(wrongTransactionId, pspTransactionId, test)
        }
    }

    @Test
    fun `reverse test transaction with no mode`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.reverse(wrongTransactionId, pspTransactionId, null)
        }
    }

    @Test
    fun `calculate PSP config`() {
        bsPayonePsp.calculatePspConfig(PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true), true)
    }

    @Test
    fun `delete alias`() {
        bsPayonePsp.deleteAlias(correctCcAliasId, test)
    }

    @Test
    fun `delete alias with wrong id`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.deleteAlias(wrongCcAliasId, test)
        }
    }
}
