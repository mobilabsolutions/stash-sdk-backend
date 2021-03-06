/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.braintree.service

import com.braintreegateway.BraintreeGateway
import com.braintreegateway.CustomerRequest
import com.braintreegateway.PayPalAccount
import com.braintreegateway.PaymentMethodRequest
import com.braintreegateway.Result
import com.braintreegateway.Transaction
import com.braintreegateway.TransactionRequest
import com.braintreegateway.exceptions.BraintreeException
import com.braintreegateway.exceptions.NotFoundException
import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.model.request.BraintreeCaptureRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreePaymentRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRefundRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeReverseRequestModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreePaymentResponseModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreeRegisterAliasResponseModel
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import mu.KLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Service
class BraintreeClient {

    companion object : KLogging()

    /**
     * Opens a Braintree  a gateway and generates a client token
     *
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return client token
     */
    fun generateClientToken(pspConfigModel: PspConfigModel, mode: String): String {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            return braintreeGateway.clientToken().generate()
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected error during Braintree client token generation").asException()
        }
    }

    /**
     * Registers payment method at Braintree
     *
     * @param request Braintree register alias request
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree payment method response
     */
    fun registerAlias(
        request: BraintreeRegisterAliasRequestModel,
        pspConfigModel: PspConfigModel,
        mode: String,
        paymentMethod: String
    ): BraintreeRegisterAliasResponseModel {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            val customerRequest = CustomerRequest().id(request.customerId)
            braintreeGateway.customer().create(customerRequest)

            val paymentMethodRequest = PaymentMethodRequest()
                .customerId(request.customerId)
                .paymentMethodNonce(request.nonce)
                .deviceData(request.deviceData)
            val paymentMethodResponse = braintreeGateway.paymentMethod().create(paymentMethodRequest)

            if (paymentMethodResponse.target == null)
                throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Registration failed, braintree.message: " + paymentMethodResponse.message).asException()

            return BraintreeRegisterAliasResponseModel(
                paymentMethodResponse.target.token,
                if (paymentMethod == PaymentMethod.PAY_PAL.name) (paymentMethodResponse.target as PayPalAccount).billingAgreementId else null
            )
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected error during registration").asException()
        }
    }

    /**
     * Deletes Braintree alias
     *
     * @param pspAlias Braintree alias
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     */
    fun deleteAlias(pspAlias: String, pspConfigModel: PspConfigModel, mode: String) {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            braintreeGateway.paymentMethod().delete(pspAlias)
        } catch (exception: NotFoundException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Alias doesn't exist at Braintree").asException()
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Error during alias deletion").asException()
        }
    }

    /**
     * Sends a preauthorization request to Braintree
     *
     * @param request Braintree payment request
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree payment response
     */
    fun preauthorization(request: BraintreePaymentRequestModel, pspConfigModel: PspConfigModel, mode: String): BraintreePaymentResponseModel {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            val transactionRequest = TransactionRequest()
                .amount(BigDecimal(request.amount).movePointLeft(2))
                .paymentMethodToken(request.token)
                .deviceData(request.deviceData)
                .options()
                .done()
            val result = braintreeGateway.transaction().sale(transactionRequest)

            return parseBraintreeResult(result)
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected error during preauthorization").asException()
        }
    }

    /**
     * Sends an authorization request to Braintree
     *
     * @param request Braintree payment request
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree payment response
     */
    fun authorization(request: BraintreePaymentRequestModel, pspConfigModel: PspConfigModel, mode: String): BraintreePaymentResponseModel {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            val paymentRequest = TransactionRequest()
                .amount(BigDecimal(request.amount).movePointLeft(2))
                .paymentMethodToken(request.token)
                .deviceData(request.deviceData)
                .options()
                .submitForSettlement(true)
                .done()
            val result = braintreeGateway.transaction().sale(paymentRequest)

            return parseBraintreeResult(result)
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected error during authorization").asException()
        }
    }

    /**
    * Sends a refund request to Braintree
    *
    * @param refundRequest Braintree refund request
    * @param pspConfigModel Braintree configuration
    * @param mode sandbox or production mode
    * @return Braintree payment response
    */
    fun refund(refundRequest: BraintreeRefundRequestModel, pspConfigModel: PspConfigModel, mode: String): BraintreePaymentResponseModel {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            val result = braintreeGateway.transaction().refund(
                refundRequest.pspTransactionId,
                BigDecimal(refundRequest.amount!!).movePointLeft(2)
            )

            return parseBraintreeResult(result)
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected error during refund").asException()
        }
    }

    /**
     * Sends a reverse request to Braintree
     *
     * @param reverseRequest Braintree reverse request
     * @param pspConfigModel Braintree configuration
     * @param mode Braintree mode
     * @return Braintree payment response
     */
    fun reverse(reverseRequest: BraintreeReverseRequestModel, pspConfigModel: PspConfigModel, mode: String): BraintreePaymentResponseModel {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            val result = braintreeGateway.transaction().voidTransaction(
                reverseRequest.pspTransactionId
            )
            return parseBraintreeResult(result)
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected error during reverse").asException()
        }
    }

    /**
     * Sends a capture request to Braintree
     *
     * @param captureRequest Braintree capture request
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree payment response
     */
    fun capture(captureRequest: BraintreeCaptureRequestModel, pspConfigModel: PspConfigModel, mode: String): BraintreePaymentResponseModel {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            val result = braintreeGateway.transaction().submitForSettlement(captureRequest.pspTransactionId)

            return parseBraintreeResult(result)
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "Unexpected error during capture").asException()
        }
    }

    /**
     * Creats a Braintree gateway based on the mode and Braintree PSP configuration
     *
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree Gateway
     */
    private fun configureBraintreeGateway(pspConfigModel: PspConfigModel, mode: String): BraintreeGateway {
        if (mode == BraintreeMode.PRODUCTION.mode)
            return BraintreeGateway(
                mode,
                pspConfigModel.merchantId,
                pspConfigModel.publicKey,
                pspConfigModel.privateKey
            )

        return BraintreeGateway(
            mode,
            pspConfigModel.sandboxMerchantId,
            pspConfigModel.sandboxPublicKey,
            pspConfigModel.sandboxPrivateKey
        )
    }

    /**
     * Parses Braintree transaction result to internal payment response model
     *
     * @param result Braintree transaction result
     * @return Braintree payment response
     */
    private fun parseBraintreeResult(result: Result<Transaction>): BraintreePaymentResponseModel {
        if (result.errors == null) {
            return BraintreePaymentResponseModel(
                status = result.target.status,
                transactionId = result.target.id
            )
        } else {
            return if (result.errors.size() == 0 && result.transaction != null) {
                BraintreePaymentResponseModel(
                    status = result.transaction.status,
                    transactionId = result.transaction.id,
                    errorCode = if (isEmptyOrNull(result.transaction.processorSettlementResponseCode))
                        result.transaction.processorResponseCode else result.transaction.processorSettlementResponseCode,
                    errorMessage = if (isEmptyOrNull(result.transaction.processorSettlementResponseText))
                        result.transaction.processorResponseText else result.transaction.processorSettlementResponseText
                )
            } else {
                return BraintreePaymentResponseModel(
                    status = null,
                    transactionId = null,
                    errorCode = result.errors.allDeepValidationErrors[0].code.code,
                    errorMessage = result.errors.allDeepValidationErrors[0].message
                )
            }
        }
    }

    private fun isEmptyOrNull(value: String?): Boolean {
        if (value != null && value.isNotEmpty())
            return false
        return true
    }
}
