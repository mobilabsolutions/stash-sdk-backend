/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.ApiKeyInfoModel
import com.mobilabsolutions.payment.model.request.ApiKeyEditRequestModel
import com.mobilabsolutions.payment.model.request.ApiKeyRequestModel
import com.mobilabsolutions.payment.model.response.ApiKeyListResponseModel
import com.mobilabsolutions.payment.model.response.ApiKeyResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
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
    @Transactional(readOnly = true)
    fun getMerchantApiKeyInfo(merchantId: String): ApiKeyListResponseModel {
        logger.info("Retrieving merchant {} keys", merchantId)
        val merchantApiKeyList = merchantApiKeyRepository.getAllByMerchantId(merchantId)
        if (merchantApiKeyList.isEmpty()) throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_EMPTY).asException()
        val apiKeyList = merchantApiKeyList.map {
            when (it.keyType) {
                KeyType.PUBLISHABLE -> ApiKeyInfoModel(
                    it.id,
                    it.name,
                    it.keyType,
                    it.key
                )
                else -> ApiKeyInfoModel(it.id, it.name, it.keyType)
            }
        }
        return ApiKeyListResponseModel(apiKeyList)
    }

    /**
     * Create merchant api key
     *
     * @param merchantId Merchant Id
     * @param apiKeyInfo Api key info request model
     * @return merchant api key method response
     */
    @Transactional
    fun createMerchantApiKey(merchantId: String, apiKeyInfo: ApiKeyRequestModel): ApiKeyResponseModel {
        logger.info("Creating merchant {} key", merchantId)
        val merchant = merchantRepository.getMerchantById(merchantId)
                ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_NOT_FOUND).asException()
        val generatedKey = merchantId + "-" + RandomStringUtils.randomAlphanumeric(API_KEY_LENGTH)

        val merchantApiKey = MerchantApiKey(
            name = apiKeyInfo.name,
            key = generatedKey,
            active = true,
            keyType = KeyType.valueOf(apiKeyInfo.type!!),
            merchant = merchant
        )
        merchantApiKeyRepository.save(merchantApiKey)

        return ApiKeyResponseModel(merchantApiKey.id, generatedKey)
    }

    /**
     * Get merchant api key
     *
     * @param apiKeyId Api Key Id
     * @return merchant api key by id method response
     */
    @Transactional(readOnly = true)
    fun getMerchantApiKeyInfoById(apiKeyId: Long): ApiKeyInfoModel {
        val merchantApiKey = merchantApiKeyRepository.getFirstById(apiKeyId)
                ?: throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()

        return when (merchantApiKey.keyType) {
            KeyType.PUBLISHABLE -> ApiKeyInfoModel(
                merchantApiKey.id,
                merchantApiKey.name,
                merchantApiKey.keyType,
                merchantApiKey.key
            )
            else -> ApiKeyInfoModel(
                merchantApiKey.id,
                merchantApiKey.name,
                merchantApiKey.keyType
            )
        }
    }

    /**
     * Edit merchant api key name
     *
     * @param apiKeyId
     * @param apiKeyInfo Api key info request model
     * @return none
     */
    @Transactional
    fun editMerchantApiKeyInfoById(apiKeyId: Long, apiKeyInfo: ApiKeyEditRequestModel) {
        if (merchantApiKeyRepository.editApiKey(apiKeyInfo.name, apiKeyId) == 0)
            throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()
    }

    /**
     * Delete merchant api key
     *
     * @param apiKeyId
     * @return none
     */
    @Transactional
    fun deleteMerchantApiKeyById(apiKeyId: Long) {
        if (merchantApiKeyRepository.deleteMerchantApiKeyById(apiKeyId) == 0)
            throw ApiError.ofErrorCode(ApiErrorCode.MERCHANT_API_KEY_NOT_FOUND).asException()
    }

    companion object : KLogging() {
        const val API_KEY_LENGTH = 20
    }
}
