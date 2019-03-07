package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.service.psp.Psp
import com.mobilabsolutions.payment.service.psp.PspRegistry
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AliasServiceTest {

    @InjectMocks
    private lateinit var aliasService: AliasService

    @Mock
    private lateinit var aliasRepository: AliasRepository

    @Mock
    private lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    private lateinit var pspRegistry: PspRegistry

    private val publicKey = "some public key"
    private val aliasId = "some alias id"
    private val pspType = "some psp type"
    private val pspAlias = "some psp alias"
    private val pspConfig =
        "{\"providers\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}, {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}"
    private val knownPspType = "BS_PAYONE"

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
            MerchantApiKey(merchant = Merchant(pspConfig = pspConfig))
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
            MerchantApiKey(merchant = Merchant(pspConfig = pspConfig))
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
            MerchantApiKey(merchant = Merchant(pspConfig = pspConfig))
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
            MerchantApiKey(merchant = Merchant(pspConfig = pspConfig))
        )
        `when`(aliasRepository.getFirstById(aliasId)).thenReturn(Alias())
        // When
        aliasService.exchangeAlias(publicKey, aliasId, AliasRequestModel(pspAlias, Mockito.mock(AliasExtraModel::class.java)))
        // Then
        verify(aliasRepository, times(1)).updateAlias(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }
}