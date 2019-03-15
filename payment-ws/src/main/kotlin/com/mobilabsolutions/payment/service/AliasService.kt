package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.message.PspConfigModel
import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.model.AliasResponseModel
import com.mobilabsolutions.payment.service.psp.PspRegistry
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

    /**
     * Creates an alias for given pspType and publicKey
     *
     * @param publicKey Public Key
     * @param pspType PSP Type
     * @return alias method response
     */
    fun createAlias(publicKey: String, pspType: String): AliasResponseModel {
        val generatedAliasId = RandomStringUtils.randomAlphanumeric(STRING_LENGTH)
        var merchantApiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey) ?: throw ApiError.ofMessage("Public Key cannot be found").asBadRequest()

        val result = objectMapper.readValue(merchantApiKey.merchant.pspConfig, Provider::class.java)
        val pspConfig = result.providers?.firstOrNull { it.type == pspType }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type ?: throw ApiError.ofMessage("PSP configuration for '$pspType' cannot be found from used merchant").asBadRequest())
        val psp = pspRegistry.find(pspConfigType) ?: throw ApiError.ofMessage("PSP implementation '$pspType' cannot be found").asBadRequest()

        val alias = Alias(
            id = generatedAliasId,
            merchant = merchantApiKey.merchant,
            psp = pspConfigType
        )
        aliasRepository.save(alias)
        val calculatedConfig = psp.calculatePspConfig(pspConfig)

        return AliasResponseModel(generatedAliasId, calculatedConfig)
    }

    /**
     * Update an alias for alias id and alias model
     *
     * @param publicKey Public Key
     * @param aliasId Alias ID
     * @param aliasRequestModel Alias Request Model
     */
    fun exchangeAlias(publicKey: String, aliasId: String, aliasRequestModel: AliasRequestModel) {
        merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey) ?: throw ApiError.ofMessage("Public Key cannot be found").asBadRequest()
        aliasRepository.getFirstById(aliasId) ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val extra = if (aliasRequestModel.extra != null) objectMapper.writeValueAsString(aliasRequestModel.extra) else null
        aliasRepository.updateAlias(aliasRequestModel.pspAlias, extra, aliasId)
    }

    private data class Provider(val providers: List<PspConfigModel>?)

    companion object : KLogging() {
        const val STRING_LENGTH = 20
    }
}