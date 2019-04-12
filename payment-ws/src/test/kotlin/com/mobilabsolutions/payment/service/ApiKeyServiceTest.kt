package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.request.ApiKeyRequestModel
import com.mobilabsolutions.payment.model.request.EditApiKeyRequestModel
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
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiKeyServiceTest {
    private val merchantApiKeys = ArrayList<MerchantApiKey>()
    private val knownApiKeyId: Long = 1
    private val unknownApiKeyId: Long = 3
    private val knownMerchantId = "mobilab"
    private val unknownMerchantId = "test"

    @InjectMocks
    private lateinit var apiKeyService: ApiKeyService

    @Mock
    private lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(merchantApiKeyRepository.getAllByMerchantId(knownMerchantId)).thenReturn(merchantApiKeys)
        Mockito.`when`(merchantRepository.getMerchantById(unknownMerchantId)).thenReturn(null)
        Mockito.`when`(merchantRepository.getMerchantById(knownMerchantId)).thenReturn(Mockito.mock(Merchant::class.java))
        Mockito.`when`(merchantApiKeyRepository.getFirstById(unknownApiKeyId)).thenReturn(null)
        Mockito.`when`(merchantApiKeyRepository.getFirstById(knownApiKeyId)).thenReturn(MerchantApiKey(merchant = Merchant()))
        Mockito.`when`(merchantApiKeyRepository.editApiKey(null, unknownApiKeyId)).thenReturn(0)
        Mockito.`when`(merchantApiKeyRepository.editApiKey(null, knownApiKeyId)).thenReturn(1)
        Mockito.`when`(merchantApiKeyRepository.deleteMerchantApiKeyById(unknownApiKeyId)).thenReturn(0)
        Mockito.`when`(merchantApiKeyRepository.deleteMerchantApiKeyById(knownApiKeyId)).thenReturn(1)
    }

    @Test
    fun `get all api key info with wrong merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfo(unknownMerchantId)
        }
    }

    @Test
    fun `get api keys successfully`() {
        merchantApiKeys.add(MerchantApiKey(merchant = Merchant()))
        apiKeyService.getMerchantApiKeyInfo(knownMerchantId)
    }

    @Test
    fun `create merchant api key with wrong merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.createMerchantApiKey(unknownMerchantId, Mockito.mock(ApiKeyRequestModel::class.java))
        }
    }

    @Test
    fun `create merchant api key successfully`() {
        apiKeyService.createMerchantApiKey(knownMerchantId, Mockito.mock(ApiKeyRequestModel::class.java))
    }

    @Test
    fun `get merchant api key info with wrong api key id`() {
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfoById(unknownApiKeyId)
        }
    }

    @Test
    fun `get merchant api key info with api key id successfully`() {
        apiKeyService.getMerchantApiKeyInfoById(knownApiKeyId)
    }

    @Test
    fun `edit merchant api key info with wrong api key id`() {
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.editMerchantApiKeyInfoById(unknownApiKeyId, Mockito.mock(EditApiKeyRequestModel::class.java))
        }
    }

    @Test
    fun `edit merchant api key info with api key id successfully`() {
        apiKeyService.editMerchantApiKeyInfoById(knownApiKeyId, Mockito.mock(EditApiKeyRequestModel::class.java))
    }

    @Test
    fun `delete merchant api key info with wrong api key id`() {
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.deleteMerchantApiKeyById(unknownApiKeyId)
        }
    }

    @Test
    fun `delete merchant api key info with api key id successfully`() {
        apiKeyService.deleteMerchantApiKeyById(knownApiKeyId)
    }
}
