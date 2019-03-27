package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.PreauthorizeRequestModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel

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

    /**
     * Returns authorization response {@link PspAuthorizeResponseModel} for the given authorization request {@link PreauthorizeRequestModel}
     * @see PspPaymentResponseModel
     * @see PreauthorizeRequestModel
     */
    fun preauthorize(preauthorizeRequestModel: PreauthorizeRequestModel): PspPaymentResponseModel
}