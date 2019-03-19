package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.MerchantRequestModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspConfigRequestModel
import com.mobilabsolutions.payment.model.PspConfigResponseModel
import com.mobilabsolutions.payment.model.PspUpsertConfigRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
**/
@Service
@Transactional
class MerchantService(
    private val merchantRepository: MerchantRepository,
    private val objectMapper: ObjectMapper,
    private val authorityRepository: AuthorityRepository
) {

    /**
     * Adds PSP configuration for the merchant based on the given merchant id.
     *
     * @param merchantId Merchant Id
     * @param pspConfigRequestModel PSP Config Request Model
     * @return psp config response model
     */
    fun addPspConfigForMerchant(merchantId: String, pspConfigRequestModel: PspConfigRequestModel): PspConfigResponseModel {
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofMessage("Merchant ID cannot be found").asBadRequest()
        val configList = if (merchant.pspConfig != null) objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java) else PspConfigListModel()
        configList.psp.add(PspConfigModel(
            type = pspConfigRequestModel.pspId,
            merchantId = pspConfigRequestModel.pspConfig.merchantId,
            portalId = pspConfigRequestModel.pspConfig.portalId,
            key = pspConfigRequestModel.pspConfig.key,
            accountId = pspConfigRequestModel.pspConfig.accountId,
            publicKey = pspConfigRequestModel.pspConfig.publicKey,
            privateKey = pspConfigRequestModel.pspConfig.privateKey
        ))
        val pspConfig = objectMapper.writeValueAsString(configList)
        merchantRepository.updateMerchant(pspConfig, merchantId)
        return PspConfigResponseModel(pspConfigRequestModel.pspId)
    }

    /**
     * Returns the PSP configuration list for the given merchant id.
     *
     * @param merchantId Merchant Id
     * @return PSP configuration list
     */
    fun getMerchantConfiguration(merchantId: String): PspConfigListModel {
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofMessage("Merchant ID cannot be found").asBadRequest()
        return if (merchant.pspConfig != null) objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java) else PspConfigListModel()
    }

    /**
     * Returns the PSP configuration for the given merchant id and psp id.
     *
     * @param merchantId Merchant Id
     * @param pspId Psp Id
     * @return PSP configuration
     */
    fun getMerchantPspConfiguration(merchantId: String, pspId: String): PspConfigModel? {
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofMessage("Merchant ID cannot be found").asBadRequest()
        val configList = if (merchant.pspConfig != null) objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java) else PspConfigListModel()
        return configList.psp.firstOrNull { it.type == pspId }
    }

    /**
     * Updates PSP configuration for the given merchant id and psp id.
     *
     * @param merchantId Merchant Id
     * @param pspId Psp Id
     * @param pspUpsertConfigRequestModel PSP Upsert Config Request Model
     */
    fun updatePspConfig(merchantId: String, pspId: String, pspUpsertConfigRequestModel: PspUpsertConfigRequestModel) {
        val merchant = merchantRepository.getMerchantById(merchantId) ?: throw ApiError.ofMessage("Merchant ID cannot be found").asBadRequest()
        val configList = if (merchant.pspConfig != null) objectMapper.readValue(merchant.pspConfig, PspConfigListModel::class.java) else PspConfigListModel()
        val pspConfig = objectMapper.writeValueAsString(updateConfig(pspId, configList, pspUpsertConfigRequestModel))
        merchantRepository.updateMerchant(pspConfig, merchantId)
    }

    private fun updateConfig(pspId: String, configList: PspConfigListModel, pspUpsertConfigRequestModel: PspUpsertConfigRequestModel): PspConfigListModel {
        val pspConfig = configList.psp.firstOrNull { it.type == pspId }
        if (pspConfig != null) {
            configList.psp.remove(pspConfig)
            configList.psp.add(PspConfigModel(
                type = pspId,
                merchantId = pspUpsertConfigRequestModel.merchantId,
                portalId = pspUpsertConfigRequestModel.portalId,
                key = pspUpsertConfigRequestModel.key,
                accountId = pspUpsertConfigRequestModel.accountId,
                publicKey = pspUpsertConfigRequestModel.publicKey,
                privateKey = pspUpsertConfigRequestModel.privateKey
            ))
        }
        return configList
    }

    /**
     * Create merchant
     *
     * @param merchantInfo Merchant specific details
     *
     */
    fun createMerchant(merchantInfo: MerchantRequestModel) {
        if (!checkMerchantAndAuthority(merchantInfo.id)) throw ApiError.ofMessage("Merchant with id '${merchantInfo.id}' already exists").asBadRequest()

        merchantRepository.save(
            Merchant(
                id = merchantInfo.id,
                name = merchantInfo.name,
                email = merchantInfo.email,
                defaultCurrency = merchantInfo.currency
            )
        )

        authorityRepository.save(Authority(name = merchantInfo.id))
    }

    private fun checkMerchantAndAuthority(merchantId: String): Boolean {
        return authorityRepository.getAuthorityByName(merchantId) == null || merchantRepository.getMerchantById(merchantId) == null
    }
}