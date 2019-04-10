package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel

interface Psp {
    /**
     * Returns the provider type {@link PaymentServiceProvider}
     *
     * @return payment service provider
     */
    fun getProvider(): PaymentServiceProvider

    /**
     * Calculates the psp alias configuration {@link PspAliasConfigModel} for the given psp configuration {@link PspConfigModel}
     *
     * @param pspConfigModel PSP configuration
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP alias configuration
     */
    fun calculatePspConfig(pspConfigModel: PspConfigModel?, pspTestMode: Boolean?): PspAliasConfigModel?

    /**
     * Returns psp preauthorization payment response {@link PspPaymentResponseModel} for the given preauthorization payment request {@link PaymentRequestModel}
     *
     * @param preauthorizeRequestModel preauthorize payment request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun preauthorize(preauthorizeRequestModel: PaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Returns psp authorize payment response {@link PspPaymentResponseModel} for the given authorization payment request {@link PaymentRequestModel}
     *
     * @param authorizeRequestModel authorize payment request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun authorize(authorizeRequestModel: PaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Returns psp capture payment response (@link PspPaymentResponseModel} for the given transaction id and psp transaction id
     *
     * @param transactionId Transaction ID
     * @param pspTransactionId PSP transaction ID
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun capture(transactionId: String, pspTransactionId: String?, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Returns psp reversal payment response (@link PspPaymentResponseModel} for the given transaction id and psp transaction id
     *
     * @param transactionId Transaction ID
     * @param pspTransactionId PSP transaction ID
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun reverse(transactionId: String, pspTransactionId: String?, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Deletes the payment method registered at PSP
     *
     * @param aliasId the id of the alias that will be deleted
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP delete alias response
     */
    fun deleteAlias(aliasId: String, pspTestMode: Boolean?)
}
