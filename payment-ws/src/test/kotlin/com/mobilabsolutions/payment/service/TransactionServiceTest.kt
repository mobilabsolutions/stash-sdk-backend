package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.AuthorizeRequestModel
import com.mobilabsolutions.payment.model.PaymentDataModel
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
import org.springframework.http.HttpStatus

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
    private val preauthStatus = TransactionAction.PREAUTH
    private val authStatus = TransactionAction.AUTH
    private val correctPaymentData = PaymentDataModel(1, "EUR", "reason")
    private val wrongPaymentData = PaymentDataModel(2, "EUR", "reason")
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
        " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}"
    private val extra =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\"}"

    @InjectMocks
    private lateinit var transactionService: TransactionService

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    private lateinit var aliasIdRepository: AliasRepository

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
        Mockito.`when`(aliasIdRepository.getFirstById(correctAliasId)).thenReturn(
            Alias(active = true, extra = extra)
        )
        Mockito.`when`(transactionRepository.getIdByIdempotentKeyAndAction(newIdempotentKey, preauthStatus)).thenReturn(null)
        Mockito.`when`(transactionRepository.getIdByIdempotentKeyAndAction(usedIdempotentKey, preauthStatus)).thenReturn(1)
        Mockito.`when`(
            transactionRepository.getIdByIdempotentKeyAndActionAndGivenBody(
                newIdempotentKey,
                preauthStatus,
                AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
            )
        ).thenReturn(1)
        Mockito.`when`(
            transactionRepository.getIdByIdempotentKeyAndActionAndGivenBody(
                newIdempotentKey,
                preauthStatus,
                AuthorizeRequestModel(correctAliasId, wrongPaymentData, purchaseId, customerId)
            )
        ).thenReturn(null)
        Mockito.`when`(transactionRepository.getIdByIdempotentKeyAndAction(newIdempotentKey, authStatus)).thenReturn(null)
        Mockito.`when`(transactionRepository.getIdByIdempotentKeyAndAction(usedIdempotentKey, authStatus)).thenReturn(1)
        Mockito.`when`(
            transactionRepository.getIdByIdempotentKeyAndActionAndGivenBody(
                newIdempotentKey,
                authStatus,
                AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId)
            )
        ).thenReturn(1)
        Mockito.`when`(
            transactionRepository.getIdByIdempotentKeyAndActionAndGivenBody(
                newIdempotentKey,
                authStatus,
                AuthorizeRequestModel(correctAliasId, wrongPaymentData, purchaseId, customerId)
            )
        ).thenReturn(null)
    }

    @Test
    fun `preauthorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(wrongSecretKey, usedIdempotentKey, Mockito.mock(AuthorizeRequestModel::class.java))
        }
    }

    @Test
    fun `preauthorize transaction with correct secret key`() {
        transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
    }

    @Test
    fun `preauthorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(wrongAliasId, correctPaymentData, purchaseId, customerId))
        }
    }

    @Test
    fun `preauthorize transaction with correct alias id`() {
        transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
    }

    @Test
    fun `preauthorize transaction with used payment data`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(wrongAliasId, wrongPaymentData, purchaseId, customerId))
        }
    }

    @Test
    fun `preauthorize transaction with new idempotent key`() {
        val responseEntity = transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
        Assertions.assertEquals(responseEntity.statusCode, HttpStatus.CREATED)
    }

    @Test
    fun `preauthorize transaction with used idempotent key`() {
        val responseEntity = transactionService.preauthorize(correctSecretKey, usedIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
        Assertions.assertEquals(responseEntity.statusCode, HttpStatus.OK)
    }

    @Test
    fun `authorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(wrongSecretKey, usedIdempotentKey, Mockito.mock(AuthorizeRequestModel::class.java))
        }
    }

    @Test
    fun `authorize transaction with correct secret key`() {
        transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
    }

    @Test
    fun `authorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(wrongAliasId, correctPaymentData, purchaseId, customerId))
        }
    }

    @Test
    fun `authorize transaction with correct alias id`() {
        transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
    }

    @Test
    fun `authorize transaction with used payment data`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(wrongAliasId, wrongPaymentData, purchaseId, customerId))
        }
    }

    @Test
    fun `authorize transaction with new idempotent key`() {
        val responseEntity = transactionService.preauthorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
        Assertions.assertEquals(responseEntity.statusCode, HttpStatus.CREATED)
    }

    @Test
    fun `authorize transaction with used idempotent key`() {
        val responseEntity = transactionService.preauthorize(correctSecretKey, usedIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
        Assertions.assertEquals(responseEntity.statusCode, HttpStatus.OK)
    }
}