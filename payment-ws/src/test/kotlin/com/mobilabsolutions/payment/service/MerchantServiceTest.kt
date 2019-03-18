package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.MerchantRequestModel
import com.mobilabsolutions.server.commons.CommonConfiguration
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class MerchantServiceTest {

    @InjectMocks
    private lateinit var merchantService: MerchantService

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Spy
    private val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @Test
    fun `create merchant with existing merchant id`() {
        `when`(merchantRepository.getMerchantById(null)).thenReturn(
                Merchant()
        )
        Assertions.assertThrows(ApiException::class.java) {
            merchantService.createMerchant(Mockito.mock(MerchantRequestModel::class.java))
        }
        verify(merchantRepository, times(0)).save(Merchant())
    }

    @Test
    fun `create merchant successfully`() {
        `when`(merchantRepository.getMerchantById(null)).thenReturn(
                null
        )
        merchantService.createMerchant(Mockito.mock(MerchantRequestModel::class.java))
        verify(merchantRepository, times(1)).save(Merchant())
    }
}