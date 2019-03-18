package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.ApiKeyRequestModel
import com.mobilabsolutions.payment.model.ApiKeyReturnInfoModel
import com.mobilabsolutions.payment.model.CreateApiKeyResponseModel
import com.mobilabsolutions.payment.model.GetApiKeyResponseModel
import com.mobilabsolutions.payment.model.EditApiKeyRequestModel
import com.mobilabsolutions.payment.model.GetApiKeyByIdResponseModel
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
        val merchantApiKeyList = merchantApiKeyRepository.getAllByMerchantId(merchantId)
        if (merchantApiKeyList.isEmpty()) throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
        val list = merchantApiKeyList.map { ApiKeyReturnInfoModel(merchantId, it.name, it.keyType) }

        return GetApiKeyResponseModel(list)
    }

    /**
     * Create merchant api key
     *
     * @param merchantId Merchant Id
     * @param apiKeyInfo Api key info request model
     * @return merchant api key method response
     */
    fun createMerchantApiKey(merchantId: String, apiKeyInfo: ApiKeyRequestModel): CreateApiKeyResponseModel {
        val merchant = merchantRepository.getMerchantById(merchantId)
                ?: throw ApiError.ofMessage("Merchant cannot be found").asBadRequest()
        val generatedKey = merchantId + "-" + RandomStringUtils.randomAlphanumeric(ApiKeyService.STRING_LENGTH)

        val merchantApiKey = MerchantApiKey(
                name = apiKeyInfo.apiKeyName,
                key = generatedKey,
                active = true,
                keyType = apiKeyInfo.apiKeyType,
                merchant = merchant
        )
        merchantApiKeyRepository.save(merchantApiKey)

        return CreateApiKeyResponseModel(generatedKey, apiKeyInfo.apiKeyType)
    }

    /**
     * Get merchant api key
     *
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
     * @param apiKeyInfo Api key info request model
     * @return none
     */
    fun editMerchantApiKeyInfoById(apiKeyId: Long, apiKeyInfo: EditApiKeyRequestModel) {
        if (merchantApiKeyRepository.editApiKey(apiKeyInfo.apiKeyName, apiKeyId) == 0)
            throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
    }

    /**
     * Delete merchant api key
     *
     * @param apiKeyId
     * @return none
     */
    fun deleteMerchantApiKeyById(apiKeyId: Long) {
        if (merchantApiKeyRepository.deleteMerchantApiKeyById(apiKeyId) == 0)
            throw ApiError.ofMessage("Merchant api key cannot be found").asBadRequest()
    }

    companion object : KLogging() {
        const val STRING_LENGTH = 20
    }
}