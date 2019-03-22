package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.domain.Transaction
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
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.MockitoAnnotations
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorizationServiceTest {
    private val correctIdempotentKey = "correct key"
    private val wrongIdempotentKey = "wrong key"
    private val correctSecretKey = "correct key"
    private val wrongSecretKey = "wrong key"
    private val correctAliasId = "correct alias id"
    private val wrongAliasId = "wrong alias id"
    private val paymentData = PaymentDataModel(1, "EUR", "reason")
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
            " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}"
    private val extra =
            "{\"email\": \"test@test.com\",\"paymentMethod\": \"SEPA\"}"

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
        `when`(transactionRepository.getTransactionByIdempotentKey(correctIdempotentKey)).thenReturn(
                Transaction(amount = 1, merchant = Merchant("1", pspConfig = pspConfig))
        )
    }

    @Test
    fun `authorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            authorizationService.authorize(wrongSecretKey, wrongIdempotentKey, Mockito.mock(AuthorizeRequestModel::class.java))
        }
    }

    @Test
    fun `authorize transaction with correct secret key`() {
        authorizationService.authorize(correctSecretKey, correctIdempotentKey, AuthorizeRequestModel(correctAliasId, paymentData, "1", "1"))
    }

    @Test
    fun `authorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            authorizationService.authorize(correctSecretKey, correctIdempotentKey, AuthorizeRequestModel(wrongAliasId, paymentData, "1", "1"))
        }
    }

    @Test
    fun `authorize transaction with correct alias id`() {
        authorizationService.authorize(correctSecretKey, correctIdempotentKey, AuthorizeRequestModel(correctAliasId, paymentData, "1", "1"))
    }
}