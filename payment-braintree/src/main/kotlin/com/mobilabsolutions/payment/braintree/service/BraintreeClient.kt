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
import com.mobilabsolutions.payment.braintree.model.request.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreePaymentResponseModel
import com.mobilabsolutions.payment.braintree.model.response.BraintreeRegisterAliasResponseModel
import com.mobilabsolutions.payment.data.enum.TransactionStatus
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
     * Makes preauthorization request at Braintree.
     *
     * @param request Braintree register alias request
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree payment method response
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
            val paymentResponse = braintreeGateway.transaction().sale(transactionRequest)

            return BraintreePaymentResponseModel(
                if (paymentResponse.isSuccess) TransactionStatus.SUCCESS else TransactionStatus.FAIL,
                getTransactionId(paymentResponse),
                null,
                null,
                null
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
     * Registers PayPal payment method at Braintree.
     *
     * @param request Braintree register alias request
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree payment method response
     */
    fun registerPayPalAlias(request: BraintreeRegisterAliasRequestModel, pspConfigModel: PspConfigModel, mode: String): BraintreeRegisterAliasResponseModel {
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
                (paymentMethodResponse.target as PayPalAccount).billingAgreementId)
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
     * Returns Braintree Gateway based on the mode and Braintree PSP configuration
     *
     * @param pspConfigModel Braintree configuration
     * @param mode sandbox or production mode
     * @return Braintree Gateway
     */
    private fun configureBraintreeGateway(pspConfigModel: PspConfigModel, mode: String): BraintreeGateway {
        if (mode == BraintreeMode.PRODUCTION.mode)
            return BraintreeGateway(mode, pspConfigModel.merchantId, pspConfigModel.publicKey, pspConfigModel.privateKey)

        return BraintreeGateway(mode, pspConfigModel.sandboxMerchantId, pspConfigModel.sandboxPublicKey, pspConfigModel.sandboxPrivateKey)
    }

    private fun getTransactionId(paymentResponse: Result<Transaction>): String? {
        when {
            paymentResponse.target != null -> return paymentResponse.target.id
            paymentResponse.transaction != null -> return paymentResponse.transaction.id
        }
        return null
    }
}
