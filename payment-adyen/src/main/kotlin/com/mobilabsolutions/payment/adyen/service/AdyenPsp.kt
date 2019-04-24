package com.mobilabsolutions.payment.adyen.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
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
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class AdyenPsp(private val adyenClient: AdyenClient) : Psp {

    companion object : KLogging()

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.ADYEN
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?, pspTestMode: Boolean?): PspAliasConfigModel? {
        logger.info { "Adyen config calculation has been called..." }
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.ADYEN.toString(),
            merchantId = null,
            portalId = null,
            request = null,
            apiVersion = null,
            responseType = null,
            hash = null,
            accountId = null,
            encoding = null,
            mode = null,
            publicKey = null,
            privateKey = null,
            clientToken = null,
            paymentSession = adyenClient.generateClientToken(pspConfigModel)
        ) else null
    }

    override fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun capture(pspCaptureRequestModel: PspCaptureRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
