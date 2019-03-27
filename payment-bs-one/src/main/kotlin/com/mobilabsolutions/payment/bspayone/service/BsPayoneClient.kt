package com.mobilabsolutions.payment.bspayone.service

import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentResponseModel
import com.mobilabsolutions.payment.model.PspConfigModel

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
interface BsPayoneClient {
    /**
     * Makes preauthorization request to BS Payone.
     *
     * @param paymentRequest BS Payone payment request
     * @param pspConfigModel BS Payone configuration
     * @return BS Payone payment response
     */
    fun preauthorization(paymentRequest: BsPayonePaymentRequestModel, pspConfigModel: PspConfigModel): BsPayonePaymentResponseModel
}