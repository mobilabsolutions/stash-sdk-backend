package com.mobilabsolutions.payment.bsone.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.message.PspAliasConfigModel
import com.mobilabsolutions.payment.message.PspConfigModel
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
            type = PaymentServiceProvider.BS_PAYONE.value,
            merchantId = pspConfigModel.merchantId,
            portalId = pspConfigModel.portalId,
            apiVersion = BsPayoneHashingService.API_VERSION,
            responseType = BsPayoneHashingService.RESPONSE_TYPE,
            hash = bsPayoneHashingService.makeCreditCardCheckHash(pspConfigModel),
            accountId = pspConfigModel.accountId,
            encoding = ENCODING,
            mode = BsPayoneHashingService.MODE,
            publicKey = null,
            privateKey = null
        ) else null
    }
}