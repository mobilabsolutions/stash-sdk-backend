package com.mobilabsolutions.payment.service.bsone

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.message.PspConfigMessage
import com.mobilabsolutions.payment.service.psp.Psp
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Component

@Component
class BsOnePsp : Psp {
    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BS_PAYONE
    }

    override fun calculatePspConfig(pspConfigMessage: PspConfigMessage): PspConfigMessage {
        logger.info { "Random config calculation has been called..." }
        return pspConfigMessage.copy(hash = RandomStringUtils.randomAlphanumeric(30))
    }

    companion object : KLogging()
}