/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.validation

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.request.DynamicPspConfigRequestModel
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class PspValidator {
    fun validate(pspType: String?, dynamicModel: DynamicPspConfigRequestModel?): Boolean {
        return when (pspType) {
            PaymentServiceProvider.ADYEN.name -> return (dynamicModel?.token != null && dynamicModel.channel != null && dynamicModel.returnUrl != null)
            else -> true
        }
    }
}
