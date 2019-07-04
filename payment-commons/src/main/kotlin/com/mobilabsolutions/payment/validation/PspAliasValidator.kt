/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.validation

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.AliasExtraModel
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class PspAliasValidator {
    fun validate(aliasModel: AliasExtraModel?, pspAlias: String?, pspType: String): Boolean {
        return when (pspType) {
            PaymentServiceProvider.BS_PAYONE.name -> {
                return when (aliasModel?.paymentMethod) {
                    PaymentMethod.CC.name -> (pspAlias != null)
                    else -> true
                }
            }
            else -> true
        }
    }
}
