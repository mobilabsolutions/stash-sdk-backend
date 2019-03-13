package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.CreateApiKeyResponseModel
import com.mobilabsolutions.payment.model.GetApiKeyByIdResponseModel
import com.mobilabsolutions.payment.model.GetApiKeyResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
@Transactional
class ApiKeyService(
    private val merchantApiKeyRepository: MerchantApiKeyRepository,
    private val merchantRepository: MerchantRepository
) {
    /**
     * Get api key info for specific merchant
     *
     * @param merchantId Merchant Id
     * @return api key method response
     */
    fun getMerchantApiKeyInfo(merchantId: String): GetApiKeyResponseModel {
        val merchantApiKey = merchantApiKeyRepository.getAllByMerchantId(merchantId)
        if (merchantApiKey.isEmpty()) throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val list: MutableList<ApiKeyReturnInfo> = mutableListOf()
        for (apiKey in merchantApiKey) {
            list.add(ApiKeyReturnInfo(merchantId, apiKey.name, apiKey.keyType))
        }

        return GetApiKeyResponseModel(list)
    }

    /**
     * Create merchant api key
     *
     * @param merchantId Merchant Id
     * @param apiKeyType Key type
     * @param apiKeyName key name
     * @return merchant api key method response
     */
    fun createMerchantApiKey(merchantId: String, apiKeyType: KeyType?, apiKeyName: String): Any {
        val merchant = merchantRepository.getMerchantById(merchantId)
                ?: throw ApiError.ofMessage("Merchant cannot be found").asBadRequest()
        val generatedKey = merchantId + "-" + RandomStringUtils.randomAlphanumeric(ApiKeyService.STRING_LENGTH)

        val merchantApiKey = MerchantApiKey(
                name = apiKeyName,
                key = generatedKey,
                active = true,
                keyType = apiKeyType,
                merchant = merchant
        )
        merchantApiKeyRepository.save(merchantApiKey)

        return CreateApiKeyResponseModel(generatedKey, apiKeyType)
    }

    /**
     * Get merchant api key
     *
     * @param merchantId Merchant Id
     * @param apiKeyId Api Key Id
     * @return merchant api key by id method response
     */
    fun getMerchantApiKeyInfoById(apiKeyId: Long): GetApiKeyByIdResponseModel {
        val merchantApiKey = merchantApiKeyRepository.getFirstById(apiKeyId)
                ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()

        return GetApiKeyByIdResponseModel(merchantApiKey.id, merchantApiKey.name, merchantApiKey.keyType)
    }

    /**
     * Edit merchant api key name
     *
     * @param apiKeyId
     * @param apiKeyName
     * @return none
     */
    fun editMerchantApiKeyInfoById(apiKeyId: Long, apiKeyName: String) {
        merchantApiKeyRepository.getFirstById(apiKeyId)
                ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        merchantApiKeyRepository.editApiKey(apiKeyName, apiKeyId)
    }

    /**
     * Delete merchant api key
     *
     * @param apiKeyId
     * @return none
     */
    fun deleteMerchantApiKeyById(apiKeyId: Long) {
        merchantApiKeyRepository.getFirstById(apiKeyId)
                ?: throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        merchantApiKeyRepository.deleteMerchantApiKeyById(apiKeyId)
    }

    data class ApiKeyReturnInfo(val merchantId: String, val apiKeyName: String?, val apiKeyType: KeyType?)

    companion object : KLogging() {
        const val STRING_LENGTH = 20
    }
}