package com.mobilabsolutions.payment.adyen.service

import com.mobilabsolutions.payment.adyen.configuration.AdyenProperties
import com.mobilabsolutions.payment.adyen.data.enum.AdyenMode
import com.mobilabsolutions.payment.adyen.data.enum.AdyenResultCode
import com.mobilabsolutions.payment.adyen.model.request.AdyenAmountRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentMethodRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenPaymentRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRecurringRequestModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenRefundRequestModel
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
            publicKey = if (adyenMode == AdyenMode.TEST.mode) pspConfigModel.sandboxPublicKey else pspConfigModel.publicKey,
            privateKey = null,
            clientToken = null,
            paymentSession = if (dynamicPspConfig != null)
                adyenClient.requestPaymentSession(pspConfigModel, dynamicPspConfig, adyenMode) else null
        ) else null
    }

    override fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        if (pspRegisterAliasRequestModel.aliasExtra == null) throw ApiError.ofErrorCode(ApiErrorCode.INCOMPLETE_ALIAS).asException()
        if (pspRegisterAliasRequestModel.aliasExtra?.paymentMethod == PaymentMethod.CC) {
            val adyenMode = getAdyenMode(pspTestMode)
            val pspConfig = pspRegisterAliasRequestModel.pspConfig
            val request = AdyenVerifyPaymentRequestModel(
                apiKey = if (adyenMode == AdyenMode.TEST.mode) pspConfig!!.sandboxPublicKey else pspConfig!!.publicKey,
                payload = pspRegisterAliasRequestModel.aliasExtra!!.payload
            )
            val response = adyenClient.verifyPayment(request, pspConfig.urlPrefix!!, getAdyenMode(pspTestMode))

            if (response.resultCode == AdyenResultCode.ERROR.result ||
                response.resultCode == AdyenResultCode.REFUSED.result ||
                response.resultCode == AdyenResultCode.CANCELLED.result) {
                logger.error("Adyen payment session verification is failed, reason {}", response.refusalReason)
                throw ApiError.builder().withErrorCode(ApiErrorCode.PSP_MODULE_ERROR)
                    .withMessage("Error during verifying Adyen payment session")
                    .withError(response.refusalReason!!).build().asException()
            }

            return PspRegisterAliasResponseModel(pspAlias = response.recurringDetailReference, registrationReference = response.shopperReference, billingAgreementId = null)
        }
        return null
    }

    override fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen authorize payment has been called for alias {} for {} mode", pspPaymentRequestModel.aliasId, adyenMode)

        val response = when {
            pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.CC -> makeCreditCardAuthorization(pspPaymentRequestModel, adyenMode)
            pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.SEPA -> makeSepaPayment(pspPaymentRequestModel, adyenMode)
            else -> throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected payment method").asException()
        }
        if (response.resultCode == AdyenResultCode.ERROR.result ||
            response.resultCode == AdyenResultCode.REFUSED.result ||
            response.resultCode == AdyenResultCode.CANCELLED.result) {
            logger.error("Adyen authorization failed, reason {}", response.refusalReason)
            return PspPaymentResponseModel(response.pspReference, TransactionStatus.FAIL, null, null, response.refusalReason)
        }

        return PspPaymentResponseModel(response.pspReference, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun capture(pspCaptureRequestModel: PspCaptureRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val adyenMode = getAdyenMode(pspTestMode)
        logger.info("Adyen refund payment has been called for {} mode", adyenMode)

        val response = when {
            pspRefundRequestModel.paymentMethod == PaymentMethod.CC -> makeCreditCardRefund(pspRefundRequestModel, adyenMode)
            pspRefundRequestModel.paymentMethod == PaymentMethod.SEPA -> makeSepaRefund(pspRefundRequestModel, adyenMode)
            else -> throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected payment method").asException()
        }
        if (response.resultCode == AdyenResultCode.ERROR.result ||
            response.resultCode == AdyenResultCode.REFUSED.result ||
            response.resultCode == AdyenResultCode.CANCELLED.result) {
            logger.error("Adyen authorization failed, reason {}", response.refusalReason)
            return PspPaymentResponseModel(response.pspReference, TransactionStatus.FAIL, null, null, response.refusalReason)
        }

        return PspPaymentResponseModel(response.pspReference, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Resolves PSP mode using passed in boolean
     *
     * @param test Boolean representing mode
     * @return Adyen mode as a string representation
     */
    private fun getAdyenMode(test: Boolean?): String {
        if (test == null || test == false) return AdyenMode.LIVE.mode
        return AdyenMode.TEST.mode
    }

    /**
     * Makes credit card authorization at Adyen
     *
     * @param pspPaymentRequestModel PSP payment request
     * @param adyenMode test or live
     * @return Adyen payment response
     */
    private fun makeCreditCardAuthorization(pspPaymentRequestModel: PspPaymentRequestModel, adyenMode: String): AdyenPaymentResponseModel {
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
            captureDelayHours = 0,
            paymentMethod = null)
        return adyenClient.authorization(request, pspPaymentRequestModel.pspConfig!!, adyenMode)
    }

    /**
     * Makes SEPA payment at Adyen
     *
     * @param pspPaymentRequestModel PSP payment request
     * @param adyenMode test or live
     * @return Adyen payment response
     */
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
                iban = pspPaymentRequestModel.extra?.sepaConfig?.iban
            )
        )
        return adyenClient.sepaPayment(request, pspPaymentRequestModel.pspConfig!!, adyenMode)
    }

    /**
     * Makes credit card refund at Adyen
     *
     * @param pspRefundRequestModel PSP refund request
     * @param adyenMode test or live
     * @return Adyen payment response
     */
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

    /**
     * Makes SEPA refund at Adyen
     *
     * @param pspRefundRequestModel PSP refund request
     * @param adyenMode test or live
     * @return Adyen payment response
     */
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
}
