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
import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.server.commons.CommonConfiguration
import com.mobilabsolutions.server.commons.exception.ApiException
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
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
    private val knownAliasId = "some alias id"
    private val unknownAliasId = "other alias id"
    private val pspType = "some psp type"
    private val pspAlias = "some psp alias"
    private val knownPspType = "BS_PAYONE"

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

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, unknownPublishableKey))
            .thenReturn(null)
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, knownPublishableKey))
            .thenReturn(MerchantApiKey(merchant = Merchant(id = "mobilab",
                pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
                " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}")))
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, unknownSecretKey))
            .thenReturn(null)
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, knownSecretKey))
            .thenReturn(MerchantApiKey(merchant = Merchant(id = "mobilab",
                pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
                " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}")))
        doNothing().`when`(aliasRepository).updateAlias(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString()
        )
        `when`(pspRegistry.find(PaymentServiceProvider.BS_PAYONE)).thenReturn(psp)

        `when`(aliasRepository.getFirstByIdAndActive(unknownAliasId, active = true)).thenReturn(null)
        `when`(aliasRepository.getFirstByIdAndActive(knownAliasId, active = true))
            .thenReturn(Alias(psp = PaymentServiceProvider.BS_PAYONE, merchant = Merchant(id = "mobilab",
            pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
            " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}")))
    }

    @Test
    fun `create alias with wrong header parameters`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(unknownPublishableKey, pspType, true)
        }
    }

    @Test
    fun `create alias with unknown pspType`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(knownPublishableKey, pspType, true)
        }
    }

    @Test
    fun `create alias successfully`() {
        aliasService.createAlias(knownPublishableKey, knownPspType, true)
    }

    @Test
    fun `exchange alias with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.exchangeAlias(knownPublishableKey, unknownAliasId, Mockito.mock(AliasRequestModel::class.java))
        }
    }

    @Test
    fun `exchange alias successfully`() {
        aliasService.exchangeAlias(knownPublishableKey, knownAliasId, AliasRequestModel(pspAlias, Mockito.mock(AliasExtraModel::class.java)))
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
