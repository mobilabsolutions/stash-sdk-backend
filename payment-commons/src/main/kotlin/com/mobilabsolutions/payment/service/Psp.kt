package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.PaymentRequestModel
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
     * Returns psp preauthorization payment response {@link PspPaymentResponseModel} for the given preauthorization payment request {@link PaymentRequestModel}
     * @see PspPaymentResponseModel
     * @see PaymentRequestModel
     */
    fun preauthorize(preauthorizeRequestModel: PaymentRequestModel): PspPaymentResponseModel

    /**
     * Returns psp authorize payment response {@link PspPaymentResponseModel} for the given authorization payment request {@link PaymentRequestModel}
     * @see PspPaymentResponseModel
     * @see PaymentRequestModel
     */
    fun authorize(authorizeRequestModel: PaymentRequestModel): PspPaymentResponseModel
}