package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.PaymentDataModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel
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
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionServiceTest {
    private val newIdempotentKey = "new key"
    private val usedIdempotentKey = "used key"
    private val correctSecretKey = "correct key"
    private val wrongSecretKey = "wrong key"
    private val correctAliasId = "correct alias id"
    private val wrongAliasId = "wrong alias id"
    private val purchaseId = "purchase id"
    private val customerId = "customer id"
    private val pspTransactionId = "325105132"
    private val correctTransactionId = "correct transaction id"
    private val wrongTransactionId = "wrong transaction id"
    private val correctTransactionIdWithoutAuth = "correct transaction id without auth"
    private val correctTransactionIdAlreadyCaptured = "already captured transaction"
    private val correctTransactionIdWrongTestMode = "prod transaction"
    private val test = true
    private val preauthAction = TransactionAction.PREAUTH
    private val authAction = TransactionAction.AUTH
    private val captureAction = TransactionAction.CAPTURE
    private val correctPaymentData = PaymentDataModel(1, "EUR", "reason")
    private val wrongPaymentData = PaymentDataModel(2, "EUR", "reason")
    private val pspResponse = "{\"pspTransactionId\":\"325105132\",\"status\":\"SUCCESS\",\"customerId\":\"160624370\"}"
    private val executedTransaction = Transaction(id = 1, pspResponse = pspResponse)
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
        " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}"
    private val extra =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\", \"personalData\": {\"lastName\": \"Mustermann\",\"city\": \"Berlin\", \"country\": \"DE\"}}"

    @InjectMocks
    private lateinit var transactionService: TransactionService

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    private lateinit var aliasIdRepository: AliasRepository

    @Mock
    private lateinit var psp: Psp

    @Mock
    private lateinit var pspRegistry: PspRegistry

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(
            merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(
                true,
                KeyType.SECRET,
                correctSecretKey
            )
        ).thenReturn(
            MerchantApiKey(active = true, merchant = Merchant("1", pspConfig = pspConfig))
        )
        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(correctAliasId, true)).thenReturn(
            Alias(active = true, extra = extra, psp = PaymentServiceProvider.BS_PAYONE)
        )
        Mockito.`when`(pspRegistry.find(PaymentServiceProvider.BS_PAYONE)).thenReturn(psp)
        Mockito.`when`(
            psp.preauthorize(
                PaymentRequestModel(
                    correctAliasId,
                    correctPaymentData,
                    purchaseId,
                    customerId
                ),
                test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(
            psp.authorize(
                PaymentRequestModel(
                    correctAliasId,
                    correctPaymentData,
                    purchaseId,
                    customerId
                ),
                test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(
            psp.capture(
                correctTransactionId,
                pspTransactionId,
                test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndAction(newIdempotentKey, preauthAction))
            .thenReturn(null)
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndAction(usedIdempotentKey, preauthAction))
            .thenReturn(executedTransaction)
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndAction(newIdempotentKey, authAction))
            .thenReturn(null)
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndAction(usedIdempotentKey, authAction))
            .thenReturn(executedTransaction)
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndAction(
                correctTransactionId,
                preauthAction,
                TransactionStatus.SUCCESS
            )
        ).thenReturn(
            Transaction(
                amount = 1,
                transactionId = correctTransactionId,
                pspTestMode = test,
                merchant = Merchant("1", pspConfig = pspConfig),
                alias = Alias(active = true, extra = extra, psp = PaymentServiceProvider.BS_PAYONE),
                pspResponse = pspResponse
            )
        )
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndAction(
                correctTransactionIdWithoutAuth,
                preauthAction
            )
        ).thenReturn(null)
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndAction(
                correctTransactionIdAlreadyCaptured,
                captureAction
            )
        )
            .thenReturn(executedTransaction)
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndAction(
                correctTransactionIdAlreadyCaptured,
                preauthAction,
                TransactionStatus.SUCCESS
            )
        ).thenReturn(
            Transaction(
                amount = 1,
                pspTestMode = test,
                merchant = Merchant("1", pspConfig = pspConfig),
                alias = Alias(active = true, extra = extra, psp = PaymentServiceProvider.BS_PAYONE),
                pspResponse = pspResponse
            )
        )
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndAction(
                correctTransactionIdWrongTestMode,
                preauthAction,
                TransactionStatus.SUCCESS
            )
        ).thenReturn(
            Transaction(
                amount = 1,
                pspTestMode = false,
                merchant = Merchant("1", pspConfig = pspConfig),
                alias = Alias(active = true, extra = extra, psp = PaymentServiceProvider.BS_PAYONE),
                pspResponse = pspResponse
            )
        )
    }

    @Test
    fun `preauthorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                wrongSecretKey,
                usedIdempotentKey,
                test,
                Mockito.mock(PaymentRequestModel::class.java)
            )
        }
    }

    @Test
    fun `preauthorize transaction with correct secret key`() {
        transactionService.preauthorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `preauthorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(wrongAliasId, correctPaymentData, purchaseId, customerId)
            )
        }
    }

    @Test
    fun `preauthorize transaction with correct alias id`() {
        transactionService.preauthorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `preauthorize transaction with used payment data`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(wrongAliasId, wrongPaymentData, purchaseId, customerId)
            )
        }
    }

    @Test
    fun `preauthorize transaction with new idempotent key`() {
        transactionService.preauthorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `preauthorize transaction with used idempotent key`() {
        transactionService.preauthorize(
            correctSecretKey,
            usedIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `authorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                wrongSecretKey,
                usedIdempotentKey,
                test,
                Mockito.mock(PaymentRequestModel::class.java)
            )
        }
    }

    @Test
    fun `authorize transaction with correct secret key`() {
        transactionService.authorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `authorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(wrongAliasId, correctPaymentData, purchaseId, customerId)
            )
        }
    }

    @Test
    fun `authorize transaction with correct alias id`() {
        transactionService.authorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `authorize transaction with used payment data`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(wrongAliasId, wrongPaymentData, purchaseId, customerId)
            )
        }
    }

    @Test
    fun `authorize transaction with new idempotent key`() {
        transactionService.authorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `authorize transaction with used idempotent key`() {
        transactionService.preauthorize(
            correctSecretKey,
            usedIdempotentKey,
            test,
            PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
        )
    }

    @Test
    fun `capture transaction with wrong transaction id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(correctSecretKey, test, wrongTransactionId)
        }
    }

    @Test
    fun `capture transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(wrongSecretKey, test, correctTransactionId)
        }
    }

    @Test
    fun `capture transaction successfully`() {
        transactionService.capture(correctSecretKey, test, correctTransactionId)
    }

    @Test
    fun `capture transaction without prior transaction of type auth`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(correctSecretKey, test, correctTransactionIdWithoutAuth)
        }
    }

    @Test
    fun `capture transaction that already has been captured`() {
        transactionService.capture(correctSecretKey, test, correctTransactionIdAlreadyCaptured)
    }

    @Test
    fun `capture transaction with wrong test mode`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(correctSecretKey, test, correctTransactionIdWrongTestMode)
        }
    }
}