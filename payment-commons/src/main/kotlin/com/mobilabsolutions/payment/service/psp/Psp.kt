package com.mobilabsolutions.payment.service.psp

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.message.PspConfigModel

interface Psp {
    fun getProvider(): PaymentServiceProvider

    fun calculatePspConfig(pspConfigModel: PspConfigModel): PspConfigModel
}