/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.service

import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.exception.BraintreeErrors
import com.mobilabsolutions.payment.braintree.model.request.BraintreeCaptureRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreePaymentRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRefundRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeReverseRequestModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspNotificationModel
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
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Component
class BraintreePsp(private val braintreeClient: BraintreeClient) : Psp {

    companion object : KLogging()

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BRAINTREE
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?, pspTestMode: Boolean?): PspAliasConfigModel? {
        logger.info { "Braintree config calculation has been called..." }
        val braintreeMode = getBraintreeMode(pspTestMode)
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.BRAINTREE.toString(),
            merchantId = null,
            portalId = null,
            request = null,
            apiVersion = null,
            responseType = null,
            hash = null,
            accountId = null,
            encoding = null,
            mode = braintreeMode,
            publicKey = null,
            privateKey = null,
            clientToken = braintreeClient.generateClientToken(pspConfigModel, braintreeMode),
            clientEncryptionKey = null
        ) else null
    }

    override fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        logger.info("Registering alias {} for {} mode", pspRegisterAliasRequestModel.aliasId, getBraintreeMode(pspTestMode))
        if (pspRegisterAliasRequestModel.aliasExtra == null) throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Alias extra cannot be found").asException()
        if (pspRegisterAliasRequestModel.aliasExtra?.paymentMethod == PaymentMethod.SEPA.name) throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "SEPA registration is not supported for Braintree").asException()
        val braintreeRequest = BraintreeRegisterAliasRequestModel(
            customerId = pspRegisterAliasRequestModel.aliasId,
            nonce = when (pspRegisterAliasRequestModel.aliasExtra?.paymentMethod) {
                PaymentMethod.PAY_PAL.name -> pspRegisterAliasRequestModel.aliasExtra?.payPalConfig!!.nonce
                else -> pspRegisterAliasRequestModel.aliasExtra?.ccConfig!!.nonce
            },
            deviceData = when (pspRegisterAliasRequestModel.aliasExtra?.paymentMethod) {
                PaymentMethod.PAY_PAL.name -> pspRegisterAliasRequestModel.aliasExtra?.payPalConfig!!.deviceData
                else -> pspRegisterAliasRequestModel.aliasExtra?.ccConfig!!.deviceData
            }
        )
        val braintreeResponse = braintreeClient.registerAlias(braintreeRequest, pspRegisterAliasRequestModel.pspConfig!!, getBraintreeMode(pspTestMode), pspRegisterAliasRequestModel.aliasExtra?.paymentMethod!!)
        return PspRegisterAliasResponseModel(braintreeResponse.token, braintreeResponse.billingAgreementId, null, null, null, null, null, null, null, null, null, null)
    }

    override fun verify3DSAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val braintreeMode = getBraintreeMode(pspTestMode)
        logger.info("Braintree preauthorize payment has been called for alias {} for {} mode", pspPaymentRequestModel.aliasId, braintreeMode)
        val request = BraintreePaymentRequestModel(
            amount = pspPaymentRequestModel.paymentData?.amount.toString(),
            token = pspPaymentRequestModel.pspAlias,
            deviceData = when (pspPaymentRequestModel.extra?.paymentMethod) {
                PaymentMethod.PAY_PAL.name -> pspPaymentRequestModel.extra?.payPalConfig!!.deviceData
                else -> pspPaymentRequestModel.extra?.ccConfig!!.deviceData
            }
        )

        val response = braintreeClient.preauthorization(request, pspPaymentRequestModel.pspConfig!!, braintreeMode)

        if (response.errorCode != null) {
            logger.error("Error during Braintree preauthorization. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, null,
                BraintreeErrors.mapResponseCode(response.errorCode), response.errorMessage)
        }
        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val braintreeMode = getBraintreeMode(pspTestMode)
        logger.info("Braintree authorize payment has been called for alias {} for {} mode", pspPaymentRequestModel.aliasId, braintreeMode)

        val request = BraintreePaymentRequestModel(
            amount = pspPaymentRequestModel.paymentData?.amount.toString(),
            token = pspPaymentRequestModel.pspAlias,
            deviceData = when (pspPaymentRequestModel.extra?.paymentMethod) {
                PaymentMethod.PAY_PAL.name -> pspPaymentRequestModel.extra?.payPalConfig!!.deviceData
                else -> pspPaymentRequestModel.extra?.ccConfig!!.deviceData
            }
        )
        val response = braintreeClient.authorization(request, pspPaymentRequestModel.pspConfig!!, braintreeMode)

        if (response.errorCode != null) {
            logger.error("Error during Braintree authorization. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, null,
                BraintreeErrors.mapResponseCode(response.errorCode), response.errorMessage)
        }
        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun capture(pspCaptureRequestModel: PspCaptureRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val braintreeMode = getBraintreeMode(pspTestMode)
        logger.info("Braintree capture payment has been called using {} mode", braintreeMode)

        val request = BraintreeCaptureRequestModel(
            pspTransactionId = pspCaptureRequestModel.pspTransactionId
        )

        val response = braintreeClient.capture(request, pspCaptureRequestModel.pspConfig, braintreeMode)

        if (response.errorCode != null) {
            logger.error("Error during Braintree capture. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, null,
                BraintreeErrors.mapResponseCode(response.errorCode), response.errorMessage)
        }
        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val braintreeMode = getBraintreeMode(pspTestMode)
        logger.info("Braintree reverse payment has been called for psp transaction {} for {} mode", pspReversalRequestModel.pspTransactionId, braintreeMode)

        val request = BraintreeReverseRequestModel(
            pspTransactionId = pspReversalRequestModel.pspTransactionId
        )

        val response = braintreeClient.reverse(request, pspReversalRequestModel.pspConfig, braintreeMode)
        if (response.errorCode != null) {
            logger.error("Error during Braintree reverse. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, null,
                BraintreeErrors.mapResponseCode(response.errorCode), response.errorMessage)
        }
        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val braintreeMode = getBraintreeMode(pspTestMode)
        logger.info("Braintree refund payment has been called for psp transaction {} for {} mode", pspRefundRequestModel.pspTransactionId, pspTestMode)

        val request = BraintreeRefundRequestModel(
            pspTransactionId = pspRefundRequestModel.pspTransactionId,
            amount = pspRefundRequestModel.amount.toString()
        )
        val response = braintreeClient.refund(request, pspRefundRequestModel.pspConfig!!, braintreeMode)

        if (response.errorCode != null) {
            logger.error("Error during Braintree refund. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, null,
                BraintreeErrors.mapResponseCode(response.errorCode), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?) {
        logger.info("Deleting alias {} for {} mode", pspDeleteAliasRequestModel.aliasId, getBraintreeMode(pspTestMode))
        braintreeClient.deleteAlias(
            pspDeleteAliasRequestModel.pspAlias!!,
            pspDeleteAliasRequestModel.pspConfig!!,
            getBraintreeMode(pspTestMode)
        )
    }

    override fun getPspNotification(pspTransactionId: String?, pspEvent: String?, pspMessage: String?): PspNotificationModel {
        TODO("not implemented")
    }

    private fun getBraintreeMode(test: Boolean?): String {
        if (test == null || test == false) return BraintreeMode.PRODUCTION.mode
        return BraintreeMode.SANDBOX.mode
    }
}
