package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
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

        val pspConfig = objectMapper.writeValueAsString(
            PspConfigListModel(
                psp = upsertPSPConfig(
                    configList.psp, PspConfigModel(
                        default = pspConfigRequestModel.pspConfig.default,
                        type = pspConfigRequestModel.pspId.toString(),
                        merchantId = pspConfigRequestModel.pspConfig.merchantId,
                        portalId = pspConfigRequestModel.pspConfig.portalId,
                        key = pspConfigRequestModel.pspConfig.key,
                        accountId = pspConfigRequestModel.pspConfig.accountId,
                        publishableKey = pspConfigRequestModel.pspConfig.publishableKey,
                        secretKey = pspConfigRequestModel.pspConfig.secretKey
                    )
                )
            )
        )
        merchantRepository.updateMerchant(pspConfig, merchantId)
        return PspConfigResponseModel(pspConfigRequestModel.pspId.toString())
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
        val pspConfig = configList.psp.firstOrNull { it.type == pspId }
        PaymentServiceProvider.valueOf(pspConfig?.type ?: throw ApiError.ofMessage("PSP configuration for '$pspId' cannot be found from used merchant").asNotFound())
        return pspConfig
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
        PaymentServiceProvider.valueOf(configList.psp.firstOrNull { it.type == pspId }?.type ?: throw ApiError.ofMessage("PSP configuration for '$pspId' cannot be found from used merchant").asBadRequest())

        val pspConfig = objectMapper.writeValueAsString(
            PspConfigListModel(
                psp = upsertPSPConfig(
                    configList.psp, PspConfigModel(
                        default = pspUpsertConfigRequestModel.default,
                        type = pspId,
                        merchantId = pspUpsertConfigRequestModel.merchantId,
                        portalId = pspUpsertConfigRequestModel.portalId,
                        key = pspUpsertConfigRequestModel.key,
                        accountId = pspUpsertConfigRequestModel.accountId,
                        publishableKey = pspUpsertConfigRequestModel.publishableKey,
                        secretKey = pspUpsertConfigRequestModel.secretKey
                    )
                )
            )
        )

        merchantRepository.updateMerchant(pspConfig, merchantId)
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

    private fun upsertPSPConfig(
        currentConfigList: MutableList<PspConfigModel>,
        pspConfigModel: PspConfigModel
    ): MutableList<PspConfigModel> {
        val configMap = mutableMapOf<String, PspConfigModel>()
        val configList = currentConfigList.map {
            when (pspConfigModel.default) {
                true -> it.setDefault(false)
                else -> it
            }
        } as MutableList<PspConfigModel>
        configList.add(pspConfigModel)
        configList.associateByTo(configMap) { it.type }

        return configMap.values.toMutableList()
    }

    private fun PspConfigModel.setDefault(default: Boolean) = PspConfigModel(type, merchantId, portalId, key, accountId, publishableKey, secretKey, default = default)
}