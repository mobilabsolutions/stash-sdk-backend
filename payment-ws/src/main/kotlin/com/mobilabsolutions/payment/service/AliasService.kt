package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.model.AliasResponseModel
import com.mobilabsolutions.payment.model.PspConfigModel
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
    private val merchantApiKeyRepository: MerchantApiKeyRepository
) {

    fun createAlias(publicKey: String, pspType: String): AliasResponseModel {
        val generatedAliasId = RandomStringUtils.randomAlphanumeric(STRING_LENGTH)
        val merchantApiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey) ?: throw IllegalArgumentException("Public Key cannot be found")

        val result = jacksonObjectMapper().readValue(merchantApiKey.merchant.pspConfig, Provider::class.java)
        val pspConfig = result.providers?.firstOrNull { it.type == pspType }

        val alias = Alias(
            id = generatedAliasId,
            merchant = merchantApiKey.merchant,
            psp = PaymentServiceProvider.valueOf(pspConfig?.type!!)
        )
        aliasRepository.save(alias)
        return AliasResponseModel(generatedAliasId, null, pspConfig)
    }

    fun exchangeAlias(publicKey: String, aliasId: String, aliasRequestModel: AliasRequestModel): AliasResponseModel {
        merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLIC, publicKey) ?: throw IllegalArgumentException("Public Key cannot be found")
        val extra = if (aliasRequestModel.extra != null) jacksonObjectMapper().writeValueAsString(aliasRequestModel.extra) else null
        aliasRepository.updateAlias(aliasRequestModel.pspAlias!!, extra!!, aliasId)
        return AliasResponseModel(aliasId, aliasRequestModel.extra, null)
    }

    private data class Provider(val providers: List<PspConfigModel>?)

    companion object : KLogging() {
        const val STRING_LENGTH = 20
    }
}