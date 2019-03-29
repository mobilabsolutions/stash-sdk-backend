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
     * @param pspTestMode indicator whether is the test mode or not
     * @see PspAliasConfigModel
     * @see PspConfigModel
     */
    fun calculatePspConfig(pspConfigModel: PspConfigModel?, pspTestMode: Boolean?): PspAliasConfigModel?

    /**
     * Returns psp preauthorization payment response {@link PspPaymentResponseModel} for the given preauthorization payment request {@link PaymentRequestModel}
     * @param pspTestMode indicator whether is the test mode or not
     * @see PspPaymentResponseModel
     * @see PaymentRequestModel
     */
    fun preauthorize(preauthorizeRequestModel: PaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel
}