package com.mobilabsolutions.payment.service.psp

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.message.PspConfigMessage

interface Psp {
    fun getProvider(): PaymentServiceProvider

    fun calculatePspConfig(pspConfigMessage: PspConfigMessage): PspConfigMessage
}