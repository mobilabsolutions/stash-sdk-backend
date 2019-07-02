package com.mobilabsolutions.payment.adyen.service

import com.mobilabsolutions.payment.adyen.configuration.AdyenProperties
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.model.request.AdyenAmountRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenCaptureRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenDeleteAliasRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentMethodRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRecurringRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRefundRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenReverseRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenVerify3DSecureRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenVerifyPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.response.AdyenPaymentResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspDeleteAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
import com.mobilabsolutions.payment.model.response.PspPaymentResponseModel
import com.mobilabsolutions.payment.model.response.PspRegisterAliasResponseModel
import com.mobilabsolutions.payment.service.Psp
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class AdyenPsp(
    private val adyenClient: AdyenClient,
    private val adyenProperties: AdyenProperties,
    private val randomStringGenerator: RandomStringGenerator
) : Psp {
    companion object : KLogging() {
        const val REFERENCE_LENGTH = 20
    }

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.ADYEN
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?, dynamicPspConfig: DynamicPspConfigRequestModel?, pspTestMode: Boolean?): PspAliasConfigModel? {
        logger.info { "Adyen config calculation has been called..." }
        val adyenMode = getAdyenMode(pspTestMode)
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.ADYEN.toString(),
            merchantId = if (adyenMode == AdyenMode.TEST.mode) pspConfigModel.sandboxMerchantId else pspConfigModel.merchantId,
            portalId = null,
            request = null,
            apiVersion = null,
            responseType = null,
            hash = null,
            accountId = null,
            encoding = null,
            mode = adyenMode,
            publicKey = null,
            privateKey = null,
            clientToken = null,
            paymentSession = if (dynamicPspConfig != null)
                adyenClient.requestPaymentSession(pspConfigModel, dynamicPspConfig, adyenMode) else null
        ) else null
    }

    override fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        if (pspRegisterAliasRequestModel.aliasExtra == null) throw ApiError.ofErrorCode(ApiErrorCode.INCOMPLETE_ALIAS).asException()
        val adyenMode = getAdyenMode(pspTestMode)
        val pspConfig = pspRegisterAliasRequestModel.pspConfig
        when (pspRegisterAliasRequestModel.aliasExtra?.paymentMethod) {
            PaymentMethod.CC.name -> registerCreditCard(pspConfig, pspRegisterAliasRequestModel, adyenMode)
            PaymentMethod.THREE_D_SECURE.name -> register3DSecure(pspConfig, pspRegisterAliasRequestModel, adyenMode)
            else -> return null
        }
        return null
    }

    override fun verifyThreeDSecure(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen verify 3D Secure payment has been called for alias {} for {} mode", pspRegisterAliasRequestModel.aliasId, adyenMode)
        val threeDSecureConfig = pspRegisterAliasRequestModel.aliasExtra?.threeDSecureConfig

        val request = AdyenVerify3DSecureRequestModel(
            paymentData = threeDSecureConfig?.paymentData,
            md = threeDSecureConfig?.md,
            paRes = threeDSecureConfig?.paRes
        )

        val response = adyenClient.verifyThreeDSecure(request, pspRegisterAliasRequestModel.pspConfig!!, adyenMode)

        if (response.errorMessage != null || response.refusalReason != null) {
            logger.error("Adyen verification is failed, reason {}", response.errorMessage ?: response.refusalReason)
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during verifying Adyen 3D Secure")
                .withError(response.errorMessage ?: response.refusalReason!!).build().asException()
        }

        return PspRegisterAliasResponseModel(response.recurringDetailReference, null, response.shopperReference, null, null, null, null, null)
    }

    override fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen preauthorization payment has been called for alias {} for {} mode", pspPaymentRequestModel.aliasId, adyenMode)

        val response = when {
            pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.CC.name
                || pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.THREE_D_SECURE.name -> makeCreditCardPayment(pspPaymentRequestModel, adyenMode, false)
            else -> throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected payment method").asException()
        }
        if (response.errorMessage != null || response.refusalReason != null) {
            logger.error("Adyen preauthorization failed, reason {}", response.errorMessage ?: response.refusalReason)
            return PspPaymentResponseModel(response.pspReference, TransactionStatus.FAIL, null, null, response.errorMessage ?: response.refusalReason)
        }

        return PspPaymentResponseModel(response.pspReference, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen authorize payment has been called for alias {} for {} mode", pspPaymentRequestModel.aliasId, adyenMode)

        val response = when {
            pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.CC.name
                || pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.THREE_D_SECURE.name -> makeCreditCardPayment(pspPaymentRequestModel, adyenMode)
            pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.SEPA.name -> makeSepaPayment(pspPaymentRequestModel, adyenMode)
            else -> throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected payment method").asException()
        }
        if (response.errorMessage != null || response.refusalReason != null) {
            logger.error("Adyen authorization failed, reason {}", response.errorMessage ?: response.refusalReason)
            return PspPaymentResponseModel(response.pspReference, TransactionStatus.FAIL, null, null, response.errorMessage ?: response.refusalReason)
        }

        return PspPaymentResponseModel(response.pspReference, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun capture(pspCaptureRequestModel: PspCaptureRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen capture payment has been called using {} mode", adyenMode)

        val request = AdyenCaptureRequestModel(
            originalReference = pspCaptureRequestModel.pspTransactionId,
            modificationAmount = AdyenAmountRequestModel(
                value = pspCaptureRequestModel.amount,
                currency = pspCaptureRequestModel.currency
            ),
            reference = pspCaptureRequestModel.purchaseId ?: randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                pspCaptureRequestModel.pspConfig.sandboxMerchantId else pspCaptureRequestModel.pspConfig.merchantId
        )

        val response = adyenClient.capture(request, pspCaptureRequestModel.pspConfig, adyenMode)
        if (response.errorMessage != null) {
            logger.error("Adyen capture failed, reason {}", response.errorMessage)
            return PspPaymentResponseModel(response.pspReference, TransactionStatus.FAIL, null, null, response.errorMessage)
        }

        return PspPaymentResponseModel(response.pspReference, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen reverse payment has been called using {} mode", adyenMode)

        val request = AdyenReverseRequestModel(
            originalReference = pspReversalRequestModel.pspTransactionId,
            reference = pspReversalRequestModel.purchaseId ?: randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                pspReversalRequestModel.pspConfig.sandboxMerchantId else pspReversalRequestModel.pspConfig.merchantId
        )

        val response = adyenClient.reverse(request, pspReversalRequestModel.pspConfig, adyenMode)
        if (response.errorMessage != null) {
            logger.error("Adyen reversal failed, reason {}", response.errorMessage)
            return PspPaymentResponseModel(response.pspReference, TransactionStatus.FAIL, null, null, response.errorMessage)
        }

        return PspPaymentResponseModel(response.pspReference, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen refund payment has been called for {} mode", adyenMode)

        val response = when {
            pspRefundRequestModel.paymentMethod == PaymentMethod.CC.name -> makeCreditCardRefund(pspRefundRequestModel, adyenMode)
            pspRefundRequestModel.paymentMethod == PaymentMethod.SEPA.name -> makeSepaRefund(pspRefundRequestModel, adyenMode)
            else -> throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected payment method").asException()
        }
        if (response.errorMessage != null) {
            logger.error("Adyen authorization failed, reason {}", response.errorMessage)
            return PspPaymentResponseModel(response.pspReference, TransactionStatus.FAIL, null, null, response.errorMessage)
        }

        return PspPaymentResponseModel(response.pspReference, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?) {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Deleting alias {} for {} mode", pspDeleteAliasRequestModel.aliasId, adyenMode)

        if (pspDeleteAliasRequestModel.paymentMethod == PaymentMethod.CC.name) {
            val request = AdyenDeleteAliasRequestModel(
                shopperReference = pspDeleteAliasRequestModel.customerReference,
                recurringDetailReference = pspDeleteAliasRequestModel.pspAlias,
                merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                    pspDeleteAliasRequestModel.pspConfig?.sandboxMerchantId else pspDeleteAliasRequestModel.pspConfig?.merchantId
            )

            adyenClient.deleteAlias(request, pspDeleteAliasRequestModel.pspConfig!!, adyenMode)
        }
    }

    private fun getAdyenMode(test: Boolean?): String {
        if (test == null || test == false) return AdyenMode.LIVE.mode
        return AdyenMode.TEST.mode
    }

    private fun makeCreditCardPayment(pspPaymentRequestModel: PspPaymentRequestModel, adyenMode: String, executeCapture: Boolean = true): AdyenPaymentResponseModel {
        val request = AdyenPaymentRequestModel(
            amount = AdyenAmountRequestModel(
                value = pspPaymentRequestModel.paymentData?.amount,
                currency = pspPaymentRequestModel.paymentData?.currency
            ),
            shopperEmail = pspPaymentRequestModel.extra?.personalData?.email,
            shopperIP = pspPaymentRequestModel.extra?.personalData?.customerIP,
            shopperReference = pspPaymentRequestModel.extra?.personalData?.customerReference,
            selectedRecurringDetailReference = pspPaymentRequestModel.pspAlias,
            recurring = AdyenRecurringRequestModel(
                contract = adyenProperties.contract
            ),
            shopperInteraction = adyenProperties.shopperInteraction,
            reference = pspPaymentRequestModel.purchaseId ?: randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                pspPaymentRequestModel.pspConfig?.sandboxMerchantId else pspPaymentRequestModel.pspConfig?.merchantId,
            captureDelayHours = if (executeCapture) 0 else null,
            paymentMethod = null,
            execute3D = null,
            returnUrl = null,
            enableRecurring = null)
        return if (executeCapture) adyenClient.authorization(request, pspPaymentRequestModel.pspConfig!!, adyenMode) else adyenClient.preauthorization(request, pspPaymentRequestModel.pspConfig!!, adyenMode)
    }

    private fun makeSepaPayment(pspPaymentRequestModel: PspPaymentRequestModel, adyenMode: String): AdyenPaymentResponseModel {
        val request = AdyenPaymentRequestModel(
            amount = AdyenAmountRequestModel(
                value = pspPaymentRequestModel.paymentData?.amount,
                currency = pspPaymentRequestModel.paymentData?.currency
            ),
            shopperEmail = null,
            shopperIP = null,
            shopperReference = null,
            selectedRecurringDetailReference = null,
            recurring = null,
            shopperInteraction = null,
            reference = pspPaymentRequestModel.purchaseId ?: randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                pspPaymentRequestModel.pspConfig?.sandboxMerchantId else pspPaymentRequestModel.pspConfig?.merchantId,
            captureDelayHours = null,
            paymentMethod = AdyenPaymentMethodRequestModel(
                type = adyenProperties.sepaPaymentMethod,
                holderName = pspPaymentRequestModel.extra?.personalData?.firstName + " " +
                    pspPaymentRequestModel.extra?.personalData?.lastName,
                iban = pspPaymentRequestModel.extra?.sepaConfig?.iban,
                encryptedCardNumber = null,
                encryptedExpiryMonth = null,
                encryptedExpiryYear = null,
                encryptedSecurityCode = null
            ),
            execute3D = null,
            returnUrl = null,
            enableRecurring = null
        )
        return adyenClient.sepaPayment(request, pspPaymentRequestModel.pspConfig!!, adyenMode)
    }

    private fun makeCreditCardRefund(pspRefundRequestModel: PspRefundRequestModel, adyenMode: String): AdyenPaymentResponseModel {
        val request = AdyenRefundRequestModel(
            originalReference = pspRefundRequestModel.pspTransactionId,
            modificationAmount = AdyenAmountRequestModel(
                value = pspRefundRequestModel.amount,
                currency = pspRefundRequestModel.currency
            ),
            reference = pspRefundRequestModel.purchaseId ?: randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                pspRefundRequestModel.pspConfig?.sandboxMerchantId else pspRefundRequestModel.pspConfig?.merchantId
        )
        return adyenClient.refund(request, pspRefundRequestModel.pspConfig!!, adyenMode)
    }

    private fun makeSepaRefund(pspRefundRequestModel: PspRefundRequestModel, adyenMode: String): AdyenPaymentResponseModel {
        val request = AdyenRefundRequestModel(
            originalReference = pspRefundRequestModel.pspTransactionId,
            modificationAmount = null,
            reference = pspRefundRequestModel.purchaseId ?: randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                pspRefundRequestModel.pspConfig?.sandboxMerchantId else pspRefundRequestModel.pspConfig?.merchantId
        )
        return adyenClient.sepaRefund(request, pspRefundRequestModel.pspConfig!!, adyenMode)
    }

    private fun registerCreditCard(pspConfig: PspConfigModel?, pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, adyenMode: String): PspRegisterAliasResponseModel {
        val request = AdyenVerifyPaymentRequestModel(
            apiKey = if (adyenMode == AdyenMode.TEST.mode) pspConfig!!.sandboxPublicKey else pspConfig!!.publicKey,
            payload = pspRegisterAliasRequestModel.aliasExtra!!.payload
        )
        val response = adyenClient.verifyPayment(request, pspConfig.urlPrefix, adyenMode)

        if (response.errorMessage != null || response.refusalReason != null) {
            logger.error("Adyen payment session verification is failed, reason {}", response.errorMessage ?: response.refusalReason)
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during verifying Adyen payment session")
                .withError(response.errorMessage ?: response.refusalReason!!).build().asException()
        }

        return PspRegisterAliasResponseModel(response.recurringDetailReference, null, response.shopperReference, null, null, null, null, null)
    }

    private fun register3DSecure(pspConfig: PspConfigModel?, pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, adyenMode: String): PspRegisterAliasResponseModel {
        val request = AdyenPaymentRequestModel(
            amount = AdyenAmountRequestModel(
                value = 0,
                currency = pspConfig?.currency
            ),
            shopperEmail = pspRegisterAliasRequestModel.aliasExtra?.personalData?.email,
            shopperIP = pspRegisterAliasRequestModel.aliasExtra?.personalData?.customerIP,
            shopperReference = pspRegisterAliasRequestModel.aliasExtra?.personalData?.customerReference,
            selectedRecurringDetailReference = null,
            recurring = null,
            shopperInteraction = null,
            reference = randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            merchantAccount = if (adyenMode == AdyenMode.TEST.mode)
                pspRegisterAliasRequestModel.pspConfig?.sandboxMerchantId else pspRegisterAliasRequestModel.pspConfig?.merchantId,
            captureDelayHours = null,
            paymentMethod = AdyenPaymentMethodRequestModel(
                type = adyenProperties.threeDSecure,
                holderName = null,
                iban = null,
                encryptedCardNumber = pspRegisterAliasRequestModel.aliasExtra?.ccConfig?.encryptedCardNumber,
                encryptedExpiryMonth = pspRegisterAliasRequestModel.aliasExtra?.ccConfig?.encryptedExpiryMonth,
                encryptedExpiryYear = pspRegisterAliasRequestModel.aliasExtra?.ccConfig?.encryptedExpiryYear,
                encryptedSecurityCode = pspRegisterAliasRequestModel.aliasExtra?.ccConfig?.encryptedSecurityCode
            ),
            execute3D = true.toString(),
            returnUrl = pspRegisterAliasRequestModel.aliasExtra?.ccConfig?.returnUrl,
            enableRecurring = true
        )
        val response = adyenClient.registerThreeDSecure(request, pspConfig!!, adyenMode)

        if (response.errorMessage != null || response.refusalReason != null) {
            logger.error("Adyen payment session verification is failed, reason {}", response.errorMessage ?: response.refusalReason)
            throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                .withMessage("Error during verifying Adyen payment session")
                .withError(response.errorMessage ?: response.refusalReason!!).build().asException()
        }

        return PspRegisterAliasResponseModel(null, null, null, response.paymentData, response.paReq, response.termUrl, response.md, response.url)
    }
}
