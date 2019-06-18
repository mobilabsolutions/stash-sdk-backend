package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.request.AliasRequestModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.server.commons.CommonConfiguration
import com.mobilabsolutions.server.commons.exception.ApiException
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import com.mobilabsolutions.server.commons.util.RequestHashing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
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
class AliasServiceTest {
    private val knownPublishableKey = "some publishable key"
    private val unknownPublishableKey = "other publishable key"
    private val knownSecretKey = "some secret key"
    private val unknownSecretKey = "other secret key"
    private val newIdempotentKey = "new key"
    private val usedIdempotentKey = "used key"
    private val knownAliasId = "some alias id"
    private val unknownAliasId = "other alias id"
    private val pspType = "some psp type"
    private val pspAlias = "some psp alias"
    private val knownPspType = "BS_PAYONE"
    private val userAgent = "Android-1.0.0"
    private val merchant = Merchant(id = "mobilab",
        pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
        " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}")
    private val extra =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\", \"personalData\": {\"lastName\": \"Mustermann\",\"city\": \"Berlin\", \"country\": \"DE\"}}"

    @InjectMocks
    private lateinit var aliasService: AliasService

    @Mock
    private lateinit var aliasRepository: AliasRepository

    @Mock
    private lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    private lateinit var pspRegistry: PspRegistry

    @Mock
    private lateinit var psp: Psp

    @Mock
    private lateinit var randomStringGenerator: RandomStringGenerator

    @Mock
    private lateinit var requestHashing: RequestHashing

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, unknownPublishableKey))
            .thenReturn(null)
        Mockito.`when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, knownPublishableKey))
            .thenReturn(MerchantApiKey(merchant = merchant))
        Mockito.`when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, unknownSecretKey))
            .thenReturn(null)
        Mockito.`when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, knownSecretKey))
            .thenReturn(MerchantApiKey(merchant = merchant))
        doNothing().`when`(aliasRepository).updateAlias(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString()
        )
        Mockito.`when`(pspRegistry.find(PaymentServiceProvider.BS_PAYONE)).thenReturn(psp)
        Mockito.`when`(
            aliasRepository.getFirstByIdAndActive(
                unknownAliasId, active = true))
            .thenReturn(null)
        Mockito.`when`(
            aliasRepository.getFirstByIdAndActive(
                knownAliasId, active = true))
            .thenReturn(Alias(psp = PaymentServiceProvider.BS_PAYONE, extra = extra, merchant = merchant))
        Mockito.`when`(
            aliasRepository.getByIdempotentKeyAndActiveAndMerchantAndPspTypeAndUserAgent(
                newIdempotentKey, true, merchant, PaymentServiceProvider.BS_PAYONE, userAgent))
            .thenReturn(null)
        Mockito.`when`(
            aliasRepository.getByIdempotentKeyAndActiveAndMerchantAndPspTypeAndUserAgent(
                usedIdempotentKey, true, merchant, PaymentServiceProvider.BS_PAYONE, userAgent))
            .thenReturn(Alias(merchant = merchant))
        Mockito.`when`(requestHashing.hashRequest(DynamicPspConfigRequestModel("test token", null, null)))
            .thenReturn("different hash")
    }

    @Test
    fun `create alias with wrong header parameters`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(unknownPublishableKey, pspType, usedIdempotentKey, userAgent, null, true)
        }
    }

    @Test
    fun `create alias with unknown pspType`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(knownPublishableKey, pspType, usedIdempotentKey, userAgent, null, true)
        }
    }

    @Test
    fun `create alias with new idempotent key`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(knownPublishableKey, pspType, newIdempotentKey, userAgent, null, true)
        }
    }

    @Test
    fun `create alias with used idempotent key`() {
        aliasService.createAlias(knownPublishableKey, knownPspType, usedIdempotentKey, userAgent, null, true)
    }

    @Test
    fun `create alias with used idempotent key and different request body`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(knownPublishableKey, knownPspType, usedIdempotentKey, userAgent, DynamicPspConfigRequestModel("test token", null, null), true)
        }
    }

    @Test
    fun `create alias successfully`() {
        aliasService.createAlias(knownPublishableKey, knownPspType, usedIdempotentKey, userAgent, null, true)
    }

    @Test
    fun `exchange alias with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.exchangeAlias(knownPublishableKey, true, userAgent, unknownAliasId, Mockito.mock(AliasRequestModel::class.java))
        }
    }

    @Test
    fun `exchange alias successfully`() {
        aliasService.exchangeAlias(knownPublishableKey, true, userAgent, knownAliasId, AliasRequestModel(pspAlias, Mockito.mock(AliasExtraModel::class.java)))
    }

    @Test
    fun `delete alias with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.deleteAlias(unknownSecretKey, true, knownAliasId)
        }
    }

    @Test
    fun `delete alias with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.deleteAlias(knownSecretKey, true, unknownAliasId)
        }
    }

    @Test
    fun `delete alias successfully`() {
        aliasService.deleteAlias(knownSecretKey, true, knownAliasId)
    }
}
