package com.mobilabsolutions.payment.braintree.service

import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.exception.BraintreeErrors
import com.mobilabsolutions.payment.braintree.model.request.BraintreePaymentRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRefundRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
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
            clientToken = braintreeClient.generateClientToken(pspConfigModel, braintreeMode)
        ) else null
    }

    override fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        logger.info("Registering PayPal alias {} for {} mode", pspRegisterAliasRequestModel.aliasId, getBraintreeMode(pspTestMode))
        if (pspRegisterAliasRequestModel.aliasExtra == null) throw ApiError.ofMessage("Alias extra cannot be found").asInternalServerError()
        if (pspRegisterAliasRequestModel.aliasExtra?.paymentMethod != PaymentMethod.PAY_PAL)
            throw ApiError.ofMessage("Only PayPal registration is supported for Braintree").asBadRequest()

        val braintreeRequest = BraintreeRegisterAliasRequestModel(
            customerId = pspRegisterAliasRequestModel.aliasId,
            nonce = pspRegisterAliasRequestModel.aliasExtra?.payPalConfig!!.nonce,
            deviceData = pspRegisterAliasRequestModel.aliasExtra?.payPalConfig!!.deviceData
        )

        val braintreeResponse = braintreeClient.registerPayPalAlias(braintreeRequest, pspRegisterAliasRequestModel.pspConfig!!, getBraintreeMode(pspTestMode))
        return PspRegisterAliasResponseModel(braintreeResponse.token, braintreeResponse.billingAgreementId)
    }

    override fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        throw ApiError.ofMessage("Preauthorization is not supported for PayPal payment. Please choose another action.").asForbidden()
    }

    override fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        logger.info { "Braintree authorize payment has been called..." }
        val braintreeMode = getBraintreeMode(pspTestMode)

        val request = BraintreePaymentRequestModel(
            amount = pspPaymentRequestModel.paymentData?.amount.toString(),
            token = pspPaymentRequestModel.pspAlias,
            deviceData = pspPaymentRequestModel.extra?.payPalConfig!!.deviceData
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
        throw ApiError.ofMessage("Capture is not supported for PayPal payment. Please choose another action.").asForbidden()
    }

    override fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        logger.info("Braintree refund payment has been called. Test mode is {}", pspTestMode.toString())
        val braintreeMode = getBraintreeMode(pspTestMode)

        val request = BraintreeRefundRequestModel(
            pspTransactionId = pspRefundRequestModel.pspTransactionId,
            amount = pspRefundRequestModel.amount.toString()
        )
        val response = braintreeClient.refund(request, pspRefundRequestModel.pspConfig, braintreeMode)

        if (response.errorCode != null) {
            logger.error("Error during Braintree refund. Error code: {}, error message: {}", response.errorCode, response.errorMessage)
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, null,
                BraintreeErrors.mapResponseCode(response.errorCode), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, null, null, null)
    }

    override fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?) {
        logger.info("Deleting PayPal alias {} for {} mode", pspDeleteAliasRequestModel.aliasId, getBraintreeMode(pspTestMode))
        braintreeClient.deletePayPalAlias(
            pspDeleteAliasRequestModel.pspAlias!!,
            pspDeleteAliasRequestModel.pspConfig!!,
            getBraintreeMode(pspTestMode)
        )
    }

    private fun getBraintreeMode(test: Boolean?): String {
        if (test == null || test == false) return BraintreeMode.PRODUCTION.mode
        return BraintreeMode.SANDBOX.mode
    }
}
