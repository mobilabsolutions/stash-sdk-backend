package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.model.AliasResponseModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
@Transactional
class AliasService(
    private val aliasRepository: AliasRepository,
    private val merchantApiKeyRepository: MerchantApiKeyRepository,
    private val pspRegistry: PspRegistry,
    private val objectMapper: ObjectMapper
) {
    companion object : KLogging() {
        const val STRING_LENGTH = 20
    }

    /**
     * Creates an alias for given pspType and publishableKey
     *
     * @param publishableKey Publishable Key
     * @param pspType PSP Type
     * @param test indicator whether is the test mode or not
     * @return alias method response
     */
    fun createAlias(publishableKey: String, pspType: String, test: Boolean?): AliasResponseModel {
        val generatedAliasId = RandomStringUtils.randomAlphanumeric(STRING_LENGTH)
        val merchantApiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, publishableKey) ?: throw ApiError.ofMessage("Publishable Key cannot be found").asBadRequest()

        val result = objectMapper.readValue(merchantApiKey.merchant.pspConfig ?: throw ApiError.ofMessage("There are no PSP configurations defined for used merchant").asInternalServerError(), PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == pspType }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type ?: throw ApiError.ofMessage("PSP configuration for '$pspType' cannot be found from used merchant").asBadRequest())
        val psp = pspRegistry.find(pspConfigType) ?: throw ApiError.ofMessage("PSP implementation '$pspType' cannot be found").asBadRequest()

        val alias = Alias(
            id = generatedAliasId,
            merchant = merchantApiKey.merchant,
            psp = pspConfigType
        )
        aliasRepository.save(alias)
        val calculatedConfig = psp.calculatePspConfig(pspConfig, test)

        return AliasResponseModel(generatedAliasId, calculatedConfig)
    }

    /**
     * Update an alias for alias id and alias model
     *
     * @param publishableKey Publishable Key
     * @param aliasId Alias ID
     * @param aliasRequestModel Alias Request Model
     */
    fun exchangeAlias(publishableKey: String, aliasId: String, aliasRequestModel: AliasRequestModel) {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, publishableKey) ?: throw ApiError.ofMessage("Publishable Key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(aliasId, true) ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        if (apiKey.merchant.id != alias.merchant?.id) throw ApiError.ofMessage("Alias does not map to correct merchant").asBadRequest()

        val extra = if (aliasRequestModel.extra != null) objectMapper.writeValueAsString(aliasRequestModel.extra) else null
        aliasRepository.updateAlias(aliasRequestModel.pspAlias, extra, aliasId)
    }

    /**
     * Delete an alias using secret key
     *
     * @param secretKey Secret key
     * @param aliasId Alias ID
     */
    fun deleteAlias(secretKey: String, aliasId: String) {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey) ?: throw ApiError.ofMessage("Secret Key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(aliasId, true) ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        if (apiKey.merchant.id != alias.merchant?.id) throw ApiError.ofMessage("Alias does not map to correct merchant").asBadRequest()

        alias.active = false
        aliasRepository.save(alias)
    }
}