package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.model.ApiKeyRequestModel
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
class ApiKeyServiceTest : AbstractServiceTest() {

    private val merchantApiKeys = ArrayList<MerchantApiKey>()
    private val apiKeyId: Long = 1

    @Test
    fun `get all api key info with wrong merchant id`() {
        `when`(merchantApiKeyRepository.getAllByMerchantId(merchantId)).thenReturn(
                merchantApiKeys
        )
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfo(merchantId)
        }
        verify(merchantApiKeyRepository, times(1)).getAllByMerchantId(ArgumentMatchers.anyString())
    }

    @Test
    fun `get api keys successfully`() {
        `when`(merchantApiKeyRepository.getAllByMerchantId(merchantId)).thenReturn(
                merchantApiKeys
        )
        merchantApiKeys.add(MerchantApiKey(merchant = Merchant()))
        apiKeyService.getMerchantApiKeyInfo(merchantId)
        verify(merchantApiKeyRepository, times(1)).getAllByMerchantId(ArgumentMatchers.anyString())
    }

    @Test
    fun `create merchant api key with wrong merchant id`() {
        `when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
                null
        )
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.createMerchantApiKey(merchantId, Mockito.mock(ApiKeyRequestModel::class.java))
        }
        verify(merchantApiKeyRepository, times(0)).save(ArgumentMatchers.any(MerchantApiKey::class.java))
    }

    @Test
    fun `create merchant api key successfully`() {
        `when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
                Merchant()
        )
        apiKeyService.createMerchantApiKey(merchantId, Mockito.mock(ApiKeyRequestModel::class.java))
        verify(merchantApiKeyRepository, times(1)).save(ArgumentMatchers.any(MerchantApiKey::class.java))
    }

    @Test
    fun `get merchant api key info with wrong api key id`() {
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                null
        )
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.getMerchantApiKeyInfoById(apiKeyId)
        }
        verify(merchantApiKeyRepository, times(1)).getFirstById(ArgumentMatchers.anyLong())
    }

    @Test
    fun `get merchant api key info with api key id successfully`() {
        `when`(merchantApiKeyRepository.getFirstById(apiKeyId)).thenReturn(
                MerchantApiKey(merchant = Merchant())
        )
        apiKeyService.getMerchantApiKeyInfoById(apiKeyId)
        verify(merchantApiKeyRepository, times(1)).getFirstById(ArgumentMatchers.anyLong())
    }

    @Test
    fun `edit merchant api key info with wrong api key id`() {
        `when`(merchantApiKeyRepository.editApiKey(null, apiKeyId)).thenReturn(
                0
        )
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.editMerchantApiKeyInfoById(apiKeyId, Mockito.mock(ApiKeyRequestModel::class.java))
        }
        verify(merchantApiKeyRepository, times(0)).editApiKey(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())
    }

    @Test
    fun `edit merchant api key info with api key id successfully`() {
        `when`(merchantApiKeyRepository.editApiKey(null, apiKeyId)).thenReturn(
                1
        )
        apiKeyService.editMerchantApiKeyInfoById(apiKeyId, Mockito.mock(ApiKeyRequestModel::class.java))
        verify(merchantApiKeyRepository, times(1)).editApiKey(ArgumentMatchers.isNull(), ArgumentMatchers.anyLong())
    }

    @Test
    fun `delete merchant api key info with wrong api key id`() {
        `when`(merchantApiKeyRepository.deleteMerchantApiKeyById(apiKeyId)).thenReturn(
                0
        )
        Assertions.assertThrows(ApiException::class.java) {
            apiKeyService.deleteMerchantApiKeyById(apiKeyId)
        }
        verify(merchantApiKeyRepository, times(1)).deleteMerchantApiKeyById(ArgumentMatchers.anyLong())
    }

    @Test
    fun `delete merchant api key info with api key id successfully`() {
        `when`(merchantApiKeyRepository.deleteMerchantApiKeyById(apiKeyId)).thenReturn(
                1
        )
        apiKeyService.deleteMerchantApiKeyById(apiKeyId)
        verify(merchantApiKeyRepository, times(1)).deleteMerchantApiKeyById(ArgumentMatchers.anyLong())
    }
}