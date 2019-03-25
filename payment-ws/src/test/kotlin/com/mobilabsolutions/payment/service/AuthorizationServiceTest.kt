package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
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
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.http.HttpStatus

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorizationServiceTest {
    private val newIdempotentKey = "new key"
    private val usedIdempotentKey = "used key"
    private val correctSecretKey = "correct key"
    private val wrongSecretKey = "wrong key"
    private val correctAliasId = "correct alias id"
    private val wrongAliasId = "wrong alias id"
    private val correctPaymentData = PaymentDataModel(1, "EUR", "reason")
    private val wrongPaymentData = PaymentDataModel(2, "EUR", "reason")
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
            " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}"
    private val extra =
            "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\"}"

    @InjectMocks
    private lateinit var authorizationService: AuthorizationService

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
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, correctSecretKey)).thenReturn(
                MerchantApiKey(active = true, merchant = Merchant("1", pspConfig = pspConfig))
        )
        `when`(aliasIdRepository.getFirstById(correctAliasId)).thenReturn(
                Alias(active = true, extra = extra)
        )
        `when`(transactionRepository.getIdByIdempotentKey(newIdempotentKey)).thenReturn(null)
        `when`(transactionRepository.getIdByIdempotentKey(usedIdempotentKey)).thenReturn(1)
        `when`(transactionRepository.getIdByIdempotentKeyAndGivenBody(newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))).thenReturn(1)
        `when`(transactionRepository.getIdByIdempotentKeyAndGivenBody(newIdempotentKey, AuthorizeRequestModel(correctAliasId, wrongPaymentData, ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))).thenReturn(null)
    }

    @Test
    fun `authorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            authorizationService.authorize(wrongSecretKey, usedIdempotentKey, Mockito.mock(AuthorizeRequestModel::class.java))
        }
    }

    @Test
    fun `authorize transaction with correct secret key`() {
        authorizationService.authorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, "1", "1"))
    }

    @Test
    fun `authorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            authorizationService.authorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(wrongAliasId, correctPaymentData, "1", "1"))
        }
    }

    @Test
    fun `authorize transaction with correct alias id`() {
        authorizationService.authorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, "1", "1"))
    }

    @Test
    fun `authorize transaction with used payment data`() {
        Assertions.assertThrows(ApiException::class.java) {
            authorizationService.authorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(wrongAliasId, wrongPaymentData, "1", "1"))
        }
    }

    @Test
    fun `authorize transaction with new idempotent key`() {
        val responseEntity = authorizationService.authorize(correctSecretKey, newIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, "1", "1"))
        Assertions.assertEquals(responseEntity.statusCode, HttpStatus.CREATED)
    }

    @Test
    fun `authorize transaction with used idempotent key`() {
        val responseEntity = authorizationService.authorize(correctSecretKey, usedIdempotentKey, AuthorizeRequestModel(correctAliasId, correctPaymentData, "1", "1"))
        Assertions.assertEquals(responseEntity.statusCode, HttpStatus.OK)
    }
}