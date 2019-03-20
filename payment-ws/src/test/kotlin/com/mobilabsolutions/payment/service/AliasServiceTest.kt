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
    private val knownPublicKey = "some public key"
    private val unknownPublicKey = "other public key"
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

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, unknownPublicKey))
            .thenReturn(null)

        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, knownPublicKey))
            .thenReturn(MerchantApiKey(merchant = Merchant(id = "mobilab",
                pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
                " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}")))

        doNothing().`when`(aliasRepository).updateAlias(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString()
        )

        `when`(pspRegistry.find(PaymentServiceProvider.BS_PAYONE)).thenReturn(Mockito.mock(Psp::class.java))

        `when`(aliasRepository.getFirstById(unknownAliasId)).thenReturn(null)

        `when`(aliasRepository.getFirstById(knownAliasId)).thenReturn(Mockito.mock(Alias::class.java))
    }

    @Test
    fun `create alias with wrong header parameters`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(unknownPublicKey, pspType)
        }
    }

    @Test
    fun `create alias with unknown pspType`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(knownPublicKey, pspType)
        }
    }

    @Test
    fun `create alias successfully`() {
        aliasService.createAlias(knownPublicKey, knownPspType)
    }

    @Test
    fun `exchange alias with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.exchangeAlias(knownPublicKey, unknownAliasId, Mockito.mock(AliasRequestModel::class.java))
        }
    }

    @Test
    fun `exchange alias successfully`() {
        aliasService.exchangeAlias(knownPublicKey, knownAliasId, AliasRequestModel(pspAlias, Mockito.mock(AliasExtraModel::class.java)))
    }
}