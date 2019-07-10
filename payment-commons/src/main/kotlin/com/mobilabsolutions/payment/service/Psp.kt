/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspNotificationModel
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspDeleteAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspRegisterAliasRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
import com.mobilabsolutions.payment.model.response.PspPaymentResponseModel
import com.mobilabsolutions.payment.model.response.PspRegisterAliasResponseModel

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
     * @param dynamicPspConfig Dynamic PSP configuration request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP alias configuration
     */
    fun calculatePspConfig(pspConfigModel: PspConfigModel?, dynamicPspConfig: DynamicPspConfigRequestModel?, pspTestMode: Boolean?): PspAliasConfigModel?

    /**
     * Returns psp register alias response {@link PspRegisterAliasResponseModel} for the given psp register alias request {@link PspRegisterAliasRequestModel}
     *
     * @param pspRegisterAliasRequestModel PSP register alias request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP register alias response
     */
    fun registerAlias(pspRegisterAliasRequestModel: PspRegisterAliasRequestModel, pspTestMode: Boolean?): PspRegisterAliasResponseModel?

    /**
     * Returns psp preauthorization payment response {@link PspPaymentResponseModel} for the given psp preauthorization payment request {@link PspPaymentRequestModel}
     *
     * @param pspPaymentRequestModel PSP payment request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun preauthorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Returns psp authorize payment response {@link PspPaymentResponseModel} for the given authorization payment request {@link PspPaymentRequestModel}
     *
     * @param pspPaymentRequestModel PSP payment request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun authorize(pspPaymentRequestModel: PspPaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Returns psp capture payment response (@link PspPaymentResponseModel} for the given psp capture request {@link PspCaptureRequestModel}
     *
     * @param pspCaptureRequestModel PSP capture request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun capture(pspCaptureRequestModel: PspCaptureRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Returns psp reversal payment response (@link PspPaymentResponseModel} for the given psp reversal request {@link PspReversalRequestModel}
     *
     * @param pspReversalRequestModel PSP reversal request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun reverse(pspReversalRequestModel: PspReversalRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Returns psp refund payment response (@link PspPaymentResponseModel} for the given psp refund request {@link PspRefundRequestModel}
     *
     * @param pspRefundRequestModel PSP refund request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP payment response
     */
    fun refund(pspRefundRequestModel: PspRefundRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel

    /**
     * Deletes PSP alias for given psp delete alias request {@link PspDeleteAliasRequestModel}
     *
     * @param pspDeleteAliasRequestModel PSP delete alias request
     * @param pspTestMode indicator whether is the test mode or not
     * @return PSP delete alias response
     */
    fun deleteAlias(pspDeleteAliasRequestModel: PspDeleteAliasRequestModel, pspTestMode: Boolean?)

    /**
     * Returns psp notification model {@link PspNotificationModel} for the given psp specific notification information
     *
     * @param pspTransactionId PSP transaction id
     * @param pspEvent PSP transaction event
     * @param pspMessage PSP notification message
     * @return PSP notification model
     */
    fun getPspNotification(pspTransactionId: String?, pspEvent: String?, pspMessage: String?): PspNotificationModel
}
