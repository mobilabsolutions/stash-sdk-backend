package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.service.psp.Psp
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
class AliasServiceTest : AbstractServiceTest() {
    private val publicKey = "some public key"
    private val aliasId = "some alias id"
    private val pspType = "some psp type"
    private val pspAlias = "some psp alias"

    @Test
    fun `create alias with wrong header parameters`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey)).thenReturn(
            null
        )
        // When
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(publicKey, pspType)
        }
        // Then
        verify(aliasRepository, times(0)).save(ArgumentMatchers.any(Alias::class.java))
    }

    @Test
    fun `create alias with unknown pspType`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey)).thenReturn(
            MerchantApiKey(merchant = merchant)
        )
        // When
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.createAlias(publicKey, pspType)
        }
        // Then
        verify(aliasRepository, times(0)).save(ArgumentMatchers.any(Alias::class.java))
    }

    @Test
    fun `create alias successfully`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey)).thenReturn(
            MerchantApiKey(merchant = merchant)
        )
        `when`(pspRegistry.find(PaymentServiceProvider.BS_PAYONE)).thenReturn(Mockito.mock(Psp::class.java))
        // When
        aliasService.createAlias(publicKey, knownPspType)
        // Then
        verify(aliasRepository, times(1)).save(ArgumentMatchers.any(Alias::class.java))
    }

    @Test
    fun `exchange alias with wrong alias id`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey)).thenReturn(
            MerchantApiKey(merchant = merchant)
        )
        `when`(aliasRepository.getFirstById(aliasId)).thenReturn(null)
        // When
        Assertions.assertThrows(ApiException::class.java) {
            aliasService.exchangeAlias(publicKey, aliasId, Mockito.mock(AliasRequestModel::class.java))
        }
        // Then
        verify(aliasRepository, times(0)).updateAlias(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun `exchange alias successfully`() {
        // Given
        `when`(merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey)).thenReturn(
            MerchantApiKey(merchant = merchant)
        )
        `when`(aliasRepository.getFirstById(aliasId)).thenReturn(Alias())
        // When
        aliasService.exchangeAlias(publicKey, aliasId, AliasRequestModel(pspAlias, Mockito.mock(AliasExtraModel::class.java)))
        // Then
        verify(aliasRepository, times(1)).updateAlias(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }
}