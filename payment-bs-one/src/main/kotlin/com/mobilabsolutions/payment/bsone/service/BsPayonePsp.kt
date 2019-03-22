package com.mobilabsolutions.payment.bsone.service

import com.mobilabsolutions.payment.bsone.enum.BsPayoneRequestType
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.service.psp.Psp
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class BsPayonePsp(private val bsPayoneHashingService: BsPayoneHashingService) : Psp {
    companion object : KLogging() {
        const val ENCODING = "UTF-8"
    }

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BS_PAYONE
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?): PspAliasConfigModel? {
        logger.info { "Random config calculation has been called..." }
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.BS_PAYONE.toString(),
            merchantId = pspConfigModel.merchantId,
            portalId = pspConfigModel.portalId,
            request = BsPayoneRequestType.CREDIT_CARD_CHECK.type,
            apiVersion = BsPayoneHashingService.API_VERSION,
            responseType = BsPayoneHashingService.RESPONSE_TYPE,
            hash = bsPayoneHashingService.makeCreditCardCheckHash(pspConfigModel),
            accountId = pspConfigModel.accountId,
            encoding = ENCODING,
            mode = BsPayoneHashingService.MODE,
            publishableKey = null,
            secretKey = null
        ) else null
    }
}