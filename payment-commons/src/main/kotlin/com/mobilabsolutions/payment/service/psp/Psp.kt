package com.mobilabsolutions.payment.service.psp

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.message.PspAliasConfigModel
import com.mobilabsolutions.payment.message.PspConfigModel

interface Psp {
    /**
     * Returns the provider type {@link PaymentServiceProvider}
     * @see PaymentServiceProvider
     */
    fun getProvider(): PaymentServiceProvider

    /**
     * Calculates the psp alias configuration {@link PspAliasConfigModel} for the given psp configuration {@link PspConfigModel}
     * @see PspAliasConfigModel
     * @see PspConfigModel
     */
    fun calculatePspConfig(pspConfigModel: PspConfigModel?): PspAliasConfigModel?
}