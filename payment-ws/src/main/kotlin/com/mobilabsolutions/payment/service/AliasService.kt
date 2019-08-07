/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.bspayone.service.BsPayonePsp
import com.mobilabsolutions.payment.data.Alias
import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.ThreeDSecureConfigModel
import com.mobilabsolutions.payment.model.request.AliasRequestModel
import com.mobilabsolutions.payment.model.request.PspDeleteAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
import com.mobilabsolutions.payment.model.request.VerifyAliasRequestModel
import com.mobilabsolutions.payment.model.response.AliasResponseModel
import com.mobilabsolutions.payment.model.response.ExchangeAliasResponseModel
import com.mobilabsolutions.payment.validation.ConfigValidator
import com.mobilabsolutions.payment.validation.PspAliasValidator
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
class AliasService(
    private val aliasRepository: AliasRepository,
    private val merchantApiKeyRepository: MerchantApiKeyRepository,
    private val pspRegistry: PspRegistry,
    private val configValidator: ConfigValidator,
    private val pspAliasValidator: PspAliasValidator,
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
     * @param userAgent User Agent
     * @param pspTestMode indicator whether is the test mode or not
     * @return alias method response
     */
    @Transactional
    fun createAlias(publishableKey: String, pspType: String, idempotentKey: String, userAgent: String?, pspTestMode: Boolean?): AliasResponseModel {
        logger.info("Creating alias for {} psp", pspType)
        val merchantApiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, publishableKey) ?: throw ApiError.ofErrorCode(ApiErrorCode.PUBLISHABLE_KEY_NOT_FOUND).asException()
        val result = objectMapper.readValue(merchantApiKey.merchant.pspConfig ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_EMPTY).asException(), PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == pspType }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_NOT_FOUND, "PSP configuration for '$pspType' cannot be found from given merchant").asException())
        val psp = pspRegistry.find(pspConfigType) ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '$pspType' cannot be found").asException()
        val calculatedConfig = psp.calculatePspConfig(pspConfig, pspTestMode)

        return executeIdempotentAliasOperation(
            merchantApiKey.merchant,
            pspConfigType,
            calculatedConfig,
            idempotentKey,
            userAgent
        )
    }

    /**
     * Update an alias for alias id and alias model
     *
     * @param publishableKey Publishable Key
     * @param pspTestMode indicator whether is the test mode or not
     * @param userAgent User Agent
     * @param aliasId Alias ID
     * @param aliasRequestModel Alias Request Model
     */
    @Transactional
    fun exchangeAlias(publishableKey: String, pspTestMode: Boolean?, userAgent: String?, aliasId: String, aliasRequestModel: AliasRequestModel): ExchangeAliasResponseModel {
        logger.info("Exchanging alias {}", aliasId)
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, publishableKey) ?: throw ApiError.ofErrorCode(ApiErrorCode.PUBLISHABLE_KEY_NOT_FOUND).asException()
        val alias = aliasRepository.getFirstByIdAndActive(aliasId, true) ?: throw ApiError.ofErrorCode(ApiErrorCode.ALIAS_NOT_FOUND).asException()
        if (apiKey.merchant.id != alias.merchant?.id) throw ApiError.ofErrorCode(ApiErrorCode.WRONG_ALIAS_MERCHANT_MAPPING).asException()
        val result = objectMapper.readValue(apiKey.merchant.pspConfig
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_EMPTY).asException(), PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == alias.psp.toString() }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_NOT_FOUND, "PSP configuration for '${alias.psp}' cannot be found from given merchant").asException())
        if (!pspAliasValidator.validate(aliasRequestModel.extra, aliasRequestModel.pspAlias, pspConfigType.name)) throw ApiError.ofErrorCode(ApiErrorCode.PSP_ALIAS_NOT_FOUND).asException()
        if (!configValidator.validate(aliasRequestModel.extra, pspConfigType.name)) throw ApiError.ofErrorCode(ApiErrorCode.CONFIG_NOT_FOUND).asException()
        val psp = pspRegistry.find(pspConfigType)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${alias.psp}' cannot be found").asException()

        val pspRegisterAliasRequest = PspRegisterAliasRequestModel(
            aliasId = aliasId,
            aliasExtra = aliasRequestModel.extra,
            pspConfig = pspConfig
        )
        val pspRegisterAliasResponse = psp.registerAlias(pspRegisterAliasRequest, pspTestMode)
        val paypalConfig = aliasRequestModel.extra?.payPalConfig?.copy(billingAgreementId = pspRegisterAliasResponse?.billingAgreementId)
        val personalConfig = aliasRequestModel.extra?.personalData?.copy(customerReference = pspRegisterAliasResponse?.registrationReference)
        val aliasExtraModel = aliasRequestModel.extra?.copy(
            payPalConfig = paypalConfig,
            personalData = personalConfig,
            threeDSecureConfig = ThreeDSecureConfigModel(paymentData = pspRegisterAliasResponse?.paymentData, fingerprintResult = null, challengeResult = null))

        println(objectMapper.writeValueAsString(pspRegisterAliasResponse))

        val pspAlias = aliasRequestModel.pspAlias ?: pspRegisterAliasResponse?.pspAlias
        val extra = if (aliasExtraModel != null) objectMapper.writeValueAsString(aliasExtraModel) else null
        aliasRepository.updateAlias(pspAlias, extra, aliasId, userAgent)
        return ExchangeAliasResponseModel(pspRegisterAliasResponse?.authenticationToken)
    }

    fun verifyAlias(publishableKey: String, pspTestMode: Boolean?, userAgent: String?, aliasId: String, verifyAliasRequest: VerifyAliasRequestModel) {
        logger.info("Verifying alias {}", aliasId)
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.PUBLISHABLE, publishableKey) ?: throw ApiError.ofErrorCode(ApiErrorCode.PUBLISHABLE_KEY_NOT_FOUND).asException()
        val alias = aliasRepository.getFirstByIdAndActive(aliasId, true) ?: throw ApiError.ofErrorCode(ApiErrorCode.ALIAS_NOT_FOUND).asException()
        if (apiKey.merchant.id != alias.merchant?.id) throw ApiError.ofErrorCode(ApiErrorCode.WRONG_ALIAS_MERCHANT_MAPPING).asException()
        val result = objectMapper.readValue(apiKey.merchant.pspConfig
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_EMPTY).asException(), PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == alias.psp.toString() }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_NOT_FOUND, "PSP configuration for '${alias.psp}' cannot be found from given merchant").asException())
        val psp = pspRegistry.find(pspConfigType)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${alias.psp}' cannot be found").asException()

        val extra = objectMapper.readValue(alias.extra, AliasExtraModel::class.java)
        val threeDSecureConfig = extra?.threeDSecureConfig?.copy(fingerprintResult = verifyAliasRequest.fingerprintResult)
        val aliasExtraModel = extra?.copy(threeDSecureConfig = threeDSecureConfig)

        val pspRegisterAliasRequest = PspRegisterAliasRequestModel(
            aliasId = aliasId,
            aliasExtra = aliasExtraModel,
            pspConfig = pspConfig
        )

        val pspResponse = psp.verifyThreeDSecure(pspRegisterAliasRequest, pspTestMode)
        val aliasExtra = when {
            pspResponse?.paymentData != null -> aliasExtraModel?.copy(threeDSecureConfig = threeDSecureConfig?.copy(paymentData = pspResponse?.paymentData))
            else -> aliasExtraModel
        }

        aliasRepository.updateAlias(pspResponse?.pspAlias, objectMapper.writeValueAsString(aliasExtra), aliasId, userAgent)
    }

    /**
     * Delete an alias using secret key
     *
     * @param secretKey Secret key
     * @param pspTestMode indicator whether is the test mode or not
     * @param aliasId Alias ID
     */
    @Transactional
    fun deleteAlias(secretKey: String, pspTestMode: Boolean?, aliasId: String) {
        BsPayonePsp.logger.info("Deleting alias {}", aliasId)
        val apiKey = merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(true, KeyType.SECRET, secretKey) ?: throw ApiError.ofErrorCode(ApiErrorCode.SECRET_KEY_NOT_FOUND).asException()
        val alias = aliasRepository.getFirstByIdAndActive(aliasId, true) ?: throw ApiError.ofErrorCode(ApiErrorCode.ALIAS_NOT_FOUND).asException()
        if (apiKey.merchant.id != alias.merchant?.id) throw ApiError.ofErrorCode(ApiErrorCode.WRONG_ALIAS_MERCHANT_MAPPING).asException()
        val aliasExtra = objectMapper.readValue(alias.extra
            ?: throw ApiError.ofErrorCode(ApiErrorCode.INCOMPLETE_ALIAS).asException(),
            AliasExtraModel::class.java)
        val result = objectMapper.readValue(apiKey.merchant.pspConfig
            ?: throw throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_EMPTY).asException(), PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == alias.psp.toString() }
        val pspConfigType = PaymentServiceProvider.valueOf(pspConfig?.type
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_CONF_FOR_MERCHANT_NOT_FOUND, "PSP configuration for '${alias.psp}' cannot be found from given merchant").asException())
        val psp = pspRegistry.find(pspConfigType)
            ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_IMPL_NOT_FOUND, "PSP implementation '${alias.psp}' cannot be found").asException()

        val pspDeleteAliasRequest = PspDeleteAliasRequestModel(
            aliasId = aliasId,
            pspAlias = alias.pspAlias,
            paymentMethod = aliasExtra.paymentMethod,
            pspConfig = pspConfig,
            customerReference = aliasExtra.personalData?.customerReference
        )
        psp.deleteAlias(pspDeleteAliasRequest, pspTestMode)

        alias.active = false
        aliasRepository.save(alias)
    }

    private fun executeIdempotentAliasOperation(
        merchant: Merchant,
        pspConfigType: PaymentServiceProvider,
        calculatedConfig: PspAliasConfigModel?,
        idempotentKey: String,
        userAgent: String?
    ): AliasResponseModel {
        val alias = aliasRepository.getByIdempotentKeyAndActiveAndMerchantAndPspTypeAndUserAgent(idempotentKey, true, merchant, pspConfigType, userAgent)
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
                    psp = pspConfigType,
                    userAgent = userAgent,
                    requestHash = null
                )
                aliasRepository.save(newAlias)

                return AliasResponseModel(
                    generatedAliasId,
                    calculatedConfig
                )
            }
        }
    }
}
