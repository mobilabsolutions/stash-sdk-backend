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
import com.braintreegateway.exceptions.TimeoutException
import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.model.request.BraintreePaymentRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRefundRequestModel
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreePaymentResponseModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreeRegisterAliasResponseModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.server.commons.exception.ApiError
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
     * Returns Braintree client token.
     *
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return client token
     */
    fun generateClientToken(pspConfigModel: PspConfigModel, mode: String): String {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            return braintreeGateway.clientToken().generate()
        } catch (exception: TimeoutException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Timeout error during Braintree client token generation").asInternalServerError()
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Unexpected error during Braintree client token generation").asInternalServerError()
        }
    }

    /**
     * Registers PayPal payment method at Braintree.
     *
     * @param request Braintree register alias request
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree payment method response
     */
    fun registerPayPalAlias(
        request: BraintreeRegisterAliasRequestModel,
        pspConfigModel: PspConfigModel,
        mode: String
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
                throw ApiError.builder()
                    .withMessage("PayPal registration failed")
                    .withProperty("braintree.message", paymentMethodResponse.message)
                    .build().asInternalServerError()

            return BraintreeRegisterAliasResponseModel(
                paymentMethodResponse.target.token,
                (paymentMethodResponse.target as PayPalAccount).billingAgreementId
            )
        } catch (exception: TimeoutException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Timeout error during PayPal registration").asInternalServerError()
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Unexpected error during PayPal registration").asInternalServerError()
        }
    }

    /**
     * Deletes PayPal alias at Braintree
     *
     * @param pspAlias Braintree PayPal alias
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     */
    fun deletePayPalAlias(pspAlias: String, pspConfigModel: PspConfigModel, mode: String) {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            braintreeGateway.paymentMethod().delete(pspAlias)
        } catch (exception: NotFoundException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("PayPal alias doesn't exist at Braintree").asInternalServerError()
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Error during PayPal alias deletion").asInternalServerError()
        }
    }

    /**
     * Makes preauthorization request to Braintree.
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
        } catch (exception: TimeoutException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Timeout error during PayPal registration").asInternalServerError()
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Unexpected error during PayPal registration").asInternalServerError()
        }
    }

    /**
     * Makes authorization request to Braintree.
     *
     * @param request Braintree payment request
     * @param pspConfigModel Braintree configuration
     * @param mode Braintree mode
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
            throw ApiError.ofMessage("Unexpected error during authorization").asInternalServerError()
        }
    }

    /** Makes refund request to Braintree
    *
    * @param refundRequest Braintree payment request
    * @param pspConfigModel Braintree configuration
    * @param mode Braintree mode
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
            throw ApiError.ofMessage("Unexpected error during refund").asInternalServerError()
        }
    }

    /**
     * Makes capture request to Braintree
     *
     * @param pspTransactionId Braintree transaction ID
     * @param pspConfigModel Braintree configuration
     * @param mode Braintree mode
     * @return Braintree payment response
     */
    fun capture(pspTransactionId: String, pspConfigModel: PspConfigModel, mode: String): BraintreePaymentResponseModel {
        try {
            val braintreeGateway = configureBraintreeGateway(pspConfigModel, mode)
            val result = braintreeGateway.transaction().submitForSettlement(pspTransactionId)

            return parseBraintreeResult(result)
        } catch (exception: BraintreeException) {
            logger.error { exception.message }
            throw ApiError.ofMessage("Unexpected error during capture").asInternalServerError()
        }
    }

    /**
     * Returns Braintree Gateway based on the mode and Braintree PSP configuration
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
                    errorCode =
                    if (result.transaction.processorSettlementResponseCode != null && result.transaction.processorSettlementResponseCode.isNotEmpty())
                        result.transaction.processorSettlementResponseCode else result.transaction.processorResponseCode,
                    errorMessage =
                    if (result.transaction.processorSettlementResponseText != null && result.transaction.processorSettlementResponseText.isNotEmpty())
                        result.transaction.processorSettlementResponseText else result.transaction.processorResponseText
                )
            } else {
                BraintreePaymentResponseModel(
                    status = null,
                    transactionId = null,
                    errorCode = result.errors.allDeepValidationErrors[0].code.code,
                    errorMessage = result.errors.allDeepValidationErrors[0].message
                )
            }
        }
    }
}
