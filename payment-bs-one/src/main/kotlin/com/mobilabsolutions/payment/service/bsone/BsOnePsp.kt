package com.mobilabsolutions.payment.service.bsone

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.message.PspConfigMessage
import com.mobilabsolutions.payment.service.psp.Psp
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class BsOnePsp : Psp {
    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BS_PAYONE
    }

    override fun calculatePspConfig(pspConfigMessage: PspConfigMessage): PspConfigMessage {
        logger.info { "Random config calculation has been called..." }
        return PspConfigMessage(
            type = PaymentServiceProvider.BS_PAYONE.name,
            merchantId = "42865",
            portalId = "2030968",
            apiVersion = "3.11",
            request = "creditcardcheck",
            responseType = "JSON",
            hash = "35996f45100c40d51cffedcddc471f8189fc3568c287871568dc6c8bae1c4d732ded416b502f6191fb6085a2d767ef6f",
            accountId = "42949",
            encoding = "UTF-8",
            mode = "test"
        ) // MOCK DATA
    }

    companion object : KLogging()
}