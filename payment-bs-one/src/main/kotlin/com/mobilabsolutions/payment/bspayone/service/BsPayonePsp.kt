package com.mobilabsolutions.payment.bspayone.service

import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneClearingType
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneMode
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneRequestType
import com.mobilabsolutions.payment.bspayone.exception.BsPayoneErrors
import com.mobilabsolutions.payment.bspayone.model.request.BsPayoneCaptureRequestModel
import com.mobilabsolutions.payment.bspayone.model.request.BsPayoneDeleteAliasRequestModel
import com.mobilabsolutions.payment.bspayone.model.request.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.bspayone.model.request.BsPayoneRefundRequestModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
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
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class BsPayonePsp(
    private val bsPayoneHashingService: BsPayoneHashingService,
    private val bsPayoneProperties: BsPayoneProperties,
    private val bsPayoneClient: BsPayoneClient,
    private val randomStringGenerator: RandomStringGenerator
) : Psp {

    companion object : KLogging() {
        const val REFERENCE_LENGTH = 10
        const val AMOUNT_FOR_CANCELLING_TRANSACTION = 0
        const val DELETE = "yes"
        const val DO_NOT_DELETE = "no"
    }

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BS_PAYONE
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?, dynamicPspConfig: DynamicPspConfigRequestModel?, pspTestMode: Boolean?): PspAliasConfigModel? {
        logger.info { "BS Payone config calculation has been called..." }
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.BS_PAYONE.toString(),
            merchantId = pspConfigModel.merchantId,
            portalId = pspConfigModel.portalId,
            request = BsPayoneRequestType.CREDIT_CARD_CHECK.type,
            apiVersion = bsPayoneProperties.apiVersion,
            responseType = BsPayoneHashingService.RESPONSE_TYPE,
            hash = bsPayoneHashingService.makeCreditCardCheckHash(pspConfigModel, getPspMode(pspTestMode)),
            accountId = pspConfigModel.accountId,
            encoding = bsPayoneProperties.encoding,
            mode = getPspMode(pspTestMode),
            publicKey = null,
            privateKey = null,
            clientToken = null,
            paymentSession = null
        ) else null
    }

    override fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        return null
    }

    override fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        logger.info("BS Payone preauthorization for {} mode", getPspMode(pspTestMode))
        val bsPayonePreauthorizeRequest = BsPayonePaymentRequestModel(
            accountId = pspPaymentRequestModel.pspConfig?.accountId,
            clearingType = getBsPayoneClearingType(pspPaymentRequestModel.extra?.paymentMethod!!),
            reference = randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            amount = pspPaymentRequestModel.paymentData!!.amount.toString(),
            currency = pspPaymentRequestModel.paymentData!!.currency,
            customerId = pspPaymentRequestModel.aliasId,
            lastName = pspPaymentRequestModel.extra?.personalData?.lastName,
            country = pspPaymentRequestModel.extra?.personalData?.country,
            city = pspPaymentRequestModel.extra?.personalData?.city,
            pspAlias = pspPaymentRequestModel.pspAlias,
            iban = null,
            bic = null
        )

        val response = bsPayoneClient.preauthorization(bsPayonePreauthorizeRequest, pspPaymentRequestModel.pspConfig!!, getPspMode(pspTestMode))

        if (response.hasError()) {
            logger.error("Error during BS Payone preauthorization. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, response.customerId,
                BsPayoneErrors.mapResponseCode(response.errorCode!!), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null, null)
    }

    override fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        logger.info("BS Payone authorization for {} mode", getPspMode(pspTestMode))
        val bsPayoneAuthorizeRequest = BsPayonePaymentRequestModel(
            accountId = pspPaymentRequestModel.pspConfig?.accountId,
            clearingType = getBsPayoneClearingType(pspPaymentRequestModel.extra?.paymentMethod!!),
            reference = randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            amount = pspPaymentRequestModel.paymentData!!.amount.toString(),
            currency = pspPaymentRequestModel.paymentData!!.currency,
            customerId = pspPaymentRequestModel.aliasId,
            lastName = pspPaymentRequestModel.extra?.personalData?.lastName,
            country = pspPaymentRequestModel.extra?.personalData?.country,
            city = pspPaymentRequestModel.extra?.personalData?.city,
            pspAlias = if (pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.CC.toString()) pspPaymentRequestModel.pspAlias else null,
            iban = if (pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.SEPA.toString()) pspPaymentRequestModel.extra?.sepaConfig?.iban else null,
            bic = if (pspPaymentRequestModel.extra?.paymentMethod == PaymentMethod.SEPA.toString()) pspPaymentRequestModel.extra?.sepaConfig?.bic else null
        )

        val response = bsPayoneClient.authorization(bsPayoneAuthorizeRequest, pspPaymentRequestModel.pspConfig!!, getPspMode(pspTestMode))

        if (response.hasError()) {
            logger.error("Error during BS Payone authorization. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, response.customerId,
                BsPayoneErrors.mapResponseCode(response.errorCode!!), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null, null)
    }

    override fun capture(pspCaptureRequestModel: PspCaptureRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        logger.info("BS Payone capture for {} mode", getPspMode(pspTestMode))
        return executeRequest(
            pspCaptureRequestModel.pspTransactionId!!,
            pspCaptureRequestModel.currency!!,
            pspCaptureRequestModel.amount!!,
            pspCaptureRequestModel.pspConfig,
            pspTestMode)
    }

    override fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        logger.info("BS Payone reverse for {} mode", getPspMode(pspTestMode))
        return executeRequest(
            pspReversalRequestModel.pspTransactionId!!,
            pspReversalRequestModel.currency!!,
            AMOUNT_FOR_CANCELLING_TRANSACTION,
            pspReversalRequestModel.pspConfig,
            pspTestMode)
    }

    override fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        logger.info("BS Payone refund for {} mode", getPspMode(pspTestMode))
        val bsPayoneRefundRequest = BsPayoneRefundRequestModel(
            pspTransactionId = pspRefundRequestModel.pspTransactionId,
            sequenceNumber = if (pspRefundRequestModel.action == TransactionAction.CAPTURE) 2 else 1,
            amount = (pspRefundRequestModel.amount!! * -1).toString(),
            currency = pspRefundRequestModel.currency
        )

        val response = bsPayoneClient.refund(bsPayoneRefundRequest, pspRefundRequestModel.pspConfig!!, getPspMode(pspTestMode))

        if (response.hasError()) {
            logger.error("Error during BS Payone refund. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, response.customerId,
                BsPayoneErrors.mapResponseCode(response.errorCode!!), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null, null)
    }

    override fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?) {
        logger.info("BS Payone alias deletion for {} mode", getPspMode(pspTestMode))
        val deleteAliasRequest = BsPayoneDeleteAliasRequestModel(
            customerId = pspDeleteAliasRequestModel.aliasId,
            deleteCardData = if (pspDeleteAliasRequestModel.paymentMethod == PaymentMethod.CC.toString()) DELETE else DO_NOT_DELETE,
            deleteBankAccountData = if (pspDeleteAliasRequestModel.paymentMethod == PaymentMethod.SEPA.toString()) DELETE else DO_NOT_DELETE
        )
        val response = bsPayoneClient.deleteAlias(deleteAliasRequest, pspDeleteAliasRequestModel.pspConfig!!, getPspMode(pspTestMode))
        if (response.hasError()) {
            logger.error("Error during BS Payone alias deletion. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
        }
    }

    private fun getPspMode(test: Boolean?): String {
        if (test == null || test == false) return BsPayoneMode.LIVE.mode
        return BsPayoneMode.TEST.mode
    }

    private fun getBsPayoneClearingType(paymentMethod: String): String {
        return when (PaymentMethod.valueOf(paymentMethod)) {
            PaymentMethod.CC -> BsPayoneClearingType.CC.type
            PaymentMethod.SEPA -> BsPayoneClearingType.SEPA.type
            else -> throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Payment method not supported for BS Payone").asException()
        }
    }

    private fun executeRequest(pspTransactionId: String, currency: String, amount: Int, pspConfig: PspConfigModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val bsPayoneCaptureRequest = BsPayoneCaptureRequestModel(
            pspTransactionId = pspTransactionId,
            amount = amount.toString(),
            currency = currency
        )

        val response = bsPayoneClient.capture(bsPayoneCaptureRequest, pspConfig, getPspMode(pspTestMode))

        if (response.hasError()) {
            logger.error("Error during BS Payone request. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, response.customerId,
                BsPayoneErrors.mapResponseCode(response.errorCode!!), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null, null)
    }
}
