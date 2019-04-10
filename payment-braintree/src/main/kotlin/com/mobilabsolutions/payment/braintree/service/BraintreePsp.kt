package com.mobilabsolutions.payment.braintree.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.braintree.data.enum.BraintreeMode
import com.mobilabsolutions.payment.braintree.model.BraintreeRegisterAliasRequestModel
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel
import com.mobilabsolutions.payment.model.PspRegisterAliasResponseModel
import com.mobilabsolutions.payment.service.Psp
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Component
class BraintreePsp(
    private val braintreeClient: BraintreeClient,
    private val aliasRepository: AliasRepository,
    private val jsonMapper: ObjectMapper
) : Psp {

    companion object : KLogging()

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BRAINTREE
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?, pspTestMode: Boolean?): PspAliasConfigModel? {
        logger.info { "Braintree config calculation has been called..." }
        val braintreeMode = getBraintreeMode(pspTestMode)
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.BRAINTREE.toString(),
            merchantId = if (braintreeMode == BraintreeMode.PRODUCTION.mode) pspConfigModel.merchantId else pspConfigModel.sandboxMerchantId,
            portalId = null,
            request = null,
            apiVersion = null,
            responseType = null,
            hash = null,
            accountId = null,
            encoding = null,
            mode = braintreeMode,
            publicKey = if (braintreeMode == BraintreeMode.PRODUCTION.mode) pspConfigModel.productionPublicKey else pspConfigModel.sandboxPublicKey,
            privateKey = if (braintreeMode == BraintreeMode.PRODUCTION.mode) pspConfigModel.productionPrivateKey else pspConfigModel.sandboxPrivateKey
        ) else null
    }

    override fun registerAlias(aliasId: String, aliasExtra: AliasExtraModel?, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        if (aliasExtra == null) throw ApiError.ofMessage("Alias extra cannot be foud").asInternalServerError()
        if (aliasExtra.paymentMethod != PaymentMethod.PAY_PAL)
            throw ApiError.ofMessage("Only PayPal registration is supported for Braintree").asBadRequest()
        if (aliasExtra.payPalConfig == null)
            throw ApiError.ofMessage("Alias was not configured properly, PayPal config is missing").asInternalServerError()
        val pspConfig = getPspConfig(getAlias(aliasId))

        val braintreeRequest = BraintreeRegisterAliasRequestModel(
            customerId = aliasId,
            nonce = aliasExtra.payPalConfig!!.nonce,
            deviceData = aliasExtra.payPalConfig!!.deviceData
        )
        val braintreeResponse = braintreeClient.registerPayPal(braintreeRequest, pspConfig, getBraintreeMode(pspTestMode))
        return PspRegisterAliasResponseModel(braintreeResponse.token, braintreeResponse.billingAgreementId)
    }

    override fun preauthorize(preauthorizeRequestModel: PaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun authorize(authorizeRequestModel: PaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun capture(transactionId: String, pspTransactionId: String?, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun reverse(transactionId: String, pspTransactionId: String?, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAlias(aliasId: String, pspTestMode: Boolean?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    private fun getBraintreeMode(test: Boolean?): String {
        if (test == null || test == false) return BraintreeMode.PRODUCTION.mode
        return BraintreeMode.SANDBOX.mode
    }

    private fun getAlias(aliasId: String): Alias {
        return aliasRepository.getFirstByIdAndActive(aliasId, true)
            ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
    }

    private fun getPspConfig(alias: Alias): PspConfigModel {
        val result = jsonMapper.readValue(alias.merchant?.pspConfig, PspConfigListModel::class.java)
        return result.psp.firstOrNull { it.type == getProvider().toString() }
            ?: throw ApiError.ofMessage("PSP configuration for '${getProvider()}' cannot be found from used merchant").asBadRequest()
    }
}
