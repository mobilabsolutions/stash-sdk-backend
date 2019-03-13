package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ApiKeyServiceTest {

    @InjectMocks
    private lateinit var apiKeyService: ApiKeyService

    @Mock
    private lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    private val merchantId = "some merchant id"
    private val pspConfig =
            "{\"providers\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}, {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}"
    private val merchantApiKeys = ArrayList<MerchantApiKey>()
    private val apiKeyType = KeyType.PUBLIC
    private val apiKeyName = "some key name"
    private val apiKeyId: Long = 1

    @Test
    fun `get all api key info with wrong merchant id`() {
        // Given
        `when`(merchantApiKeyRepository.getAllByMerchantId(merchantId)).thenReturn(
                merchantApiKeys
        )
        // When
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfo(merchantId)
        }
        // Then
        verify(merchantApiKeyRepository, times(1)).getAllByMerchantId(ArgumentMatchers.anyString())
    }

    @Test
    fun `get api keys successfully`() {
        // Given
        `when`(merchantApiKeyRepository.getAllByMerchantId(merchantId)).thenReturn(
                merchantApiKeys
        )
        // When
        merchantApiKeys.add(MerchantApiKey(merchant = Merchant(pspConfig = pspConfig)))
        apiKeyService.getMerchantApiKeyInfo(merchantId)
        // Then
        verify(merchantApiKeyRepository, times(1)).getAllByMerchantId(ArgumentMatchers.anyString())
    }

    @Test
    fun `create merchant api key with wrong merchant id`() {
        // Given
        `when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
                null
        )
        // When
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.createMerchantApiKey(merchantId, apiKeyType, apiKeyName)
        }
        // Then
        verify(merchantApiKeyRepository, times(0)).save(ArgumentMatchers.any(MerchantApiKey::class.java))
    }

    @Test
    fun `create merchant api key successfully`() {
        // Given
        `when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
                Merchant(pspConfig = pspConfig)
        )
        // When
        apiKeyService.createMerchantApiKey(merchantId, apiKeyType, apiKeyName)
        // Then
        verify(merchantApiKeyRepository, times(1)).save(ArgumentMatchers.any(MerchantApiKey::class.java))
    }

    @Test
    fun `get merchant api key info with wrong api key id`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                null
        )
        // When
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfoById(apiKeyId)
        }
        // Then
        verify(merchantApiKeyRepository, times(1)).getFirstById(ArgumentMatchers.anyLong())
    }

    @Test
    fun `get merchant api key info with api key id successfully`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                MerchantApiKey(merchant = Merchant(pspConfig = pspConfig))
        )
        // When
        apiKeyService.getMerchantApiKeyInfoById(apiKeyId)
        // Then
        verify(merchantApiKeyRepository, times(1)).getFirstById(ArgumentMatchers.anyLong())
    }

    @Test
    fun `edit merchant api key info with wrong api key id`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                null
        )
        // When
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfoById(apiKeyId)
        }
        // Then
        verify(merchantApiKeyRepository, times(0)).editApiKey(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())
    }

    @Test
    fun `edit merchant api key info with api key id successfully`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                MerchantApiKey(merchant = Merchant(pspConfig = pspConfig))
        )
        // When
        apiKeyService.editMerchantApiKeyInfoById(apiKeyId, apiKeyName)
        // Then
        verify(merchantApiKeyRepository, times(1)).editApiKey(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())
    }

    @Test
    fun `delete merchant api key info with wrong api key id`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                null
        )
        // When
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfoById(apiKeyId)
        }
        // Then
        verify(merchantApiKeyRepository, times(0)).editApiKey(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())
    }

    @Test
    fun `delete merchant api key info with api key id successfully`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                MerchantApiKey(merchant = Merchant(pspConfig = pspConfig))
        )
        // When
        apiKeyService.deleteMerchantApiKeyById(apiKeyId)
        // Then
        verify(merchantApiKeyRepository, times(1)).deleteMerchantApiKeyById(ArgumentMatchers.anyLong())
    }
}