package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.MerchantRequestModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigRequestModel
import com.mobilabsolutions.payment.model.PspUpsertConfigRequestModel
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
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MerchantServiceTest {
    private val pspId = "BRAINTREE"
    private val knownPspType = "BS_PAYONE"
    private val knownMerchantId = "mobilab"
    private val unknownMerchantId = "test"
    private var merchant = Merchant()

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @InjectMocks
    private lateinit var merchantService: MerchantService

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Mock
    private lateinit var authorityRepository: AuthorityRepository

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        merchant = Merchant(id = knownMerchantId,
            pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
                " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}")

        `when`(merchantRepository.getMerchantById(knownMerchantId)).thenReturn(merchant)

        `when`(merchantRepository.getMerchantById(unknownMerchantId)).thenReturn(null)

        doNothing().`when`(merchantRepository).updateMerchant(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString()
        )

        `when`(authorityRepository.getAuthorityByName(knownMerchantId)).thenReturn(
            Mockito.mock(Authority::class.java)
        )

        `when`(authorityRepository.getAuthorityByName(unknownMerchantId)).thenReturn(null)
    }

    @Test
    fun `add psp config successfully`() {
        merchantService.addPspConfigForMerchant(knownMerchantId, PspConfigRequestModel(pspId, Mockito.mock(PspUpsertConfigRequestModel::class.java)))
    }

    @Test
    fun `add psp config with wrong merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            merchantService.addPspConfigForMerchant(unknownMerchantId, PspConfigRequestModel(pspId, Mockito.mock(PspUpsertConfigRequestModel::class.java)))
        }
    }

    @Test
    fun `create merchant with existing merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            merchantService.createMerchant(MerchantRequestModel(knownMerchantId, "test", "test@mobilabsolutions.com", "EUR"))
        }
    }

    @Test
    fun `get merchant config successfully`() {
        val response = merchantService.getMerchantConfiguration(knownMerchantId)
        val config = objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java)
        Assertions.assertEquals(response.psp.size, config.psp.size)
    }

    @Test
    fun `get merchant psp config successfully`() {
        val response = merchantService.getMerchantPspConfiguration(knownMerchantId, knownPspType)
        val config = objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java).psp.firstOrNull { it.type == knownPspType }
        Assertions.assertNotNull(config)
        Assertions.assertEquals(response?.type, config?.type)
    }

    @Test
    fun `update merchant psp config successfully`() {
        merchantService.updatePspConfig(knownMerchantId, knownPspType, Mockito.mock(PspUpsertConfigRequestModel::class.java))
    }

    @Test
    fun `create merchant successfully`() {
        merchantService.createMerchant(MerchantRequestModel(unknownMerchantId, "test", "test@mobilabsolutions.com", "EUR"))
    }
}