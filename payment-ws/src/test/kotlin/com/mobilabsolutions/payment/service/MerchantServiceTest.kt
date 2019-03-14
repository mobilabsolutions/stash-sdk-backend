package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigRequestModel
import com.mobilabsolutions.payment.model.PspUpsertConfigRequestModel
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
class MerchantServiceTest : AbstractServiceTest() {
    private val pspId = "BRAINTREE"

    @Test
    fun `add psp config successfully`() {
        Mockito.`when`(merchantRepository.getFirstById(merchantId)).thenReturn(merchant)

        merchantService.addPspConfigForMerchant(merchantId, PspConfigRequestModel(pspId, Mockito.mock(PspUpsertConfigRequestModel::class.java)))

        Mockito.verify(merchantRepository, Mockito.times(1)).updateMerchant(
            ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun `add psp config with wrong merchant id`() {
        Mockito.`when`(merchantRepository.getFirstById(merchantId)).thenReturn(null)

        Assertions.assertThrows(ApiException::class.java) {
            merchantService.addPspConfigForMerchant(merchantId, PspConfigRequestModel(pspId, Mockito.mock(PspUpsertConfigRequestModel::class.java)))
        }
        Mockito.verify(merchantRepository, Mockito.times(0)).updateMerchant(
            ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun `get merchant config successfully`() {
        Mockito.`when`(merchantRepository.getFirstById(merchantId)).thenReturn(merchant)

        val response = merchantService.getMerchantConfiguration(merchantId)
        val config = objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java)
        Assertions.assertEquals(response.psp.size, config.psp.size)
    }

    @Test
    fun `get merchant psp config successfully`() {
        Mockito.`when`(merchantRepository.getFirstById(merchantId)).thenReturn(merchant)

        val response = merchantService.getMerchantPspConfiguration(merchantId, knownPspType)
        val config = objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java).psp.firstOrNull { it.type == knownPspType }
        Assertions.assertNotNull(config)
        Assertions.assertEquals(response?.type, config?.type)
    }

    @Test
    fun `update merchant psp config successfully`() {
        Mockito.`when`(merchantRepository.getFirstById(merchantId)).thenReturn(merchant)

        merchantService.updatePspConfig(merchantId, knownPspType, Mockito.mock(PspUpsertConfigRequestModel::class.java))

        Mockito.verify(merchantRepository, Mockito.times(1)).updateMerchant(
            ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }
}