package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.MerchantRequestModel
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
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
class MerchantServiceTest {

    @InjectMocks
    private lateinit var merchantService: MerchantService

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Mock
    private lateinit var authorityRepository: AuthorityRepository

    private val knownMerchantId = "known merchant"
    private val unknownMerchantId = "unknown merchant"

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)
        `when`(merchantRepository.getMerchantById(knownMerchantId)).thenReturn(
            Mockito.mock(Merchant::class.java)
        )
        `when`(authorityRepository.getAuthorityByName(knownMerchantId)).thenReturn(
            Mockito.mock(Authority::class.java)
        )
        `when`(merchantRepository.getMerchantById(unknownMerchantId)).thenReturn(
            null
        )
        `when`(authorityRepository.getAuthorityByName(unknownMerchantId)).thenReturn(
            null
        )
    }

    @Test
    fun `create merchant with existing merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            merchantService.createMerchant(MerchantRequestModel(knownMerchantId, "test", "test@mobilabsolutions.com", "EUR"))
        }
    }

    @Test
    fun `create merchant successfully`() {
        merchantService.createMerchant(MerchantRequestModel(unknownMerchantId, "test", "test@mobilabsolutions.com", "EUR"))
    }
}