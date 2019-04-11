package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.model.AliasResponseModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import mu.KLogging
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
    private val randomStringGenerator: RandomStringGenerator,
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
     * @param idempotentKey Idempotent key
     * @param pspTestMode indicator whether is the test mode or not
     * @return alias method response
     */
    fun createAlias(publishableKey: String, pspType: String, idempotentKey: String, pspTestMode: Boolean?): AliasResponseModel {
        val merchantApiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, publishableKey) ?: throw ApiError.ofMessage("Publishable Key cannot be found").asBadRequest()

        val result = objectMapper.readValue(merchantApiKey.merchant.pspConfig ?: throw ApiError.ofMessage("There are no PSP configurations defined for used merchant").asInternalServerError(), PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == pspType }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type ?: throw ApiError.ofMessage("PSP configuration for '$pspType' cannot be found from used merchant").asBadRequest())
        val psp = pspRegistry.find(pspConfigType) ?: throw ApiError.ofMessage("PSP implementation '$pspType' cannot be found").asBadRequest()
        val calculatedConfig = psp.calculatePspConfig(pspConfig, pspTestMode)

        return executeIdempotentAliasOperation(
            merchantApiKey.merchant,
            pspConfigType,
            calculatedConfig,
            idempotentKey
        )
    }

    /**
     * Update an alias for alias id and alias model
     *
     * @param publishableKey Publishable Key
     * @param pspTestMode indicator whether is the test mode or not
     * @param aliasId Alias ID
     * @param aliasRequestModel Alias Request Model
     */
    fun exchangeAlias(publishableKey: String, pspTestMode: Boolean?, aliasId: String, aliasRequestModel: AliasRequestModel) {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, publishableKey) ?: throw ApiError.ofMessage("Publishable Key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(aliasId, true) ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        if (apiKey.merchant.id != alias.merchant?.id) throw ApiError.ofMessage("Alias does not map to correct merchant").asBadRequest()
        val psp = findPsp(apiKey, alias)
        val pspRegisterAliasResponse = psp.registerAlias(aliasId, aliasRequestModel.extra, pspTestMode)
        val paypalConfig = aliasRequestModel.extra?.payPalConfig?.copy(billingAgreementId = pspRegisterAliasResponse?.billingAgreementId)
        val aliasExtraModel = aliasRequestModel.extra?.copy(payPalConfig = paypalConfig)

        val pspAlias = aliasRequestModel.pspAlias ?: pspRegisterAliasResponse?.pspAlias
        val extra = if (aliasExtraModel != null) objectMapper.writeValueAsString(aliasExtraModel) else null
        aliasRepository.updateAlias(pspAlias, extra, aliasId)
    }

    /**
     * Delete an alias using secret key
     *
     * @param secretKey Secret key
     * @param pspTestMode indicator whether is the test mode or not
     * @param aliasId Alias ID
     */
    fun deleteAlias(secretKey: String, pspTestMode: Boolean?, aliasId: String) {
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey) ?: throw ApiError.ofMessage("Secret Key cannot be found").asBadRequest()
        val alias = aliasRepository.getFirstByIdAndActive(aliasId, true) ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        if (apiKey.merchant.id != alias.merchant?.id) throw ApiError.ofMessage("Alias does not map to correct merchant").asBadRequest()
        val psp = findPsp(apiKey, alias)

        psp.deleteAlias(aliasId, pspTestMode)

        alias.active = false
        aliasRepository.save(alias)
    }

    /**
     * Execute idempotent operation alias creation
     *
     * @param merchant Merchant
     * @param pspConfigType PSP Config Type
     * @param calculatedConfig Calculated Config Type
     * @param idempotentKey Idempotent key
     * @return alias method response
     */
    private fun executeIdempotentAliasOperation(
        merchant: Merchant,
        pspConfigType: PaymentServiceProvider,
        calculatedConfig: PspAliasConfigModel?,
        idempotentKey: String
    ): AliasResponseModel {
        val alias = aliasRepository.getByIdempotentKeyAndActiveAndMerchant(idempotentKey, true, merchant)
        val generatedAliasId = randomStringGenerator.generateRandomAlphanumeric(STRING_LENGTH)

        when {
            alias != null -> return AliasResponseModel(
                alias.id,
                calculatedConfig
            )

            else -> {
                val newAlias = Alias(
                    id = generatedAliasId,
                    idempotentKey = idempotentKey,
                    merchant = merchant,
                    psp = pspConfigType
                )
                aliasRepository.save(newAlias)

                return AliasResponseModel(
                    generatedAliasId,
                    calculatedConfig
                )
            }
        }
    }

    /**
     * Finds the corresponding PSP for the given api key and alias
     *
     * @param apiKey merchant api key
     * @param alias alias
     * @return PSP
     */
    private fun findPsp(apiKey: MerchantApiKey, alias: Alias): Psp {
        val result = objectMapper.readValue(apiKey.merchant.pspConfig
            ?: throw ApiError.ofMessage("There are no PSP configurations defined for used merchant").asInternalServerError(), PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == alias.psp.toString() }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type
            ?: throw ApiError.ofMessage("PSP configuration for '${alias.psp}' cannot be found from used merchant").asBadRequest())
        return pspRegistry.find(pspConfigType)
            ?: throw ApiError.ofMessage("PSP implementation '${alias.psp}' cannot be found").asBadRequest()
    }
}
