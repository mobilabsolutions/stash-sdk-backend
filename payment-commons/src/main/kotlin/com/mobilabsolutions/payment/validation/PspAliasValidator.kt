package com.mobilabsolutions.payment.validation

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class PspAliasValidator {
    fun validate(pspAlias: String?, pspType: String): Boolean {
        return when (pspType) {
            PaymentServiceProvider.BS_PAYONE.name -> (pspAlias != null)
            else -> true
        }
    }
}
