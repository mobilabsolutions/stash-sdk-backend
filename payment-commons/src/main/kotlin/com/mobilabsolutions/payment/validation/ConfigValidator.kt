package com.mobilabsolutions.payment.validation

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.CreditCardConfigModel
import com.mobilabsolutions.payment.model.PayPalConfigModel
import com.mobilabsolutions.payment.model.SepaConfigModel
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class ConfigValidator {
    fun validate(aliasModel: AliasExtraModel?): Boolean {
        return when (aliasModel!!.paymentMethod) {
            PaymentMethod.CC.name -> checkCcData(aliasModel.ccConfig)
            PaymentMethod.SEPA.name -> checkSepaData(aliasModel.sepaConfig)
            PaymentMethod.PAY_PAL.name -> checkPaypalData(aliasModel.payPalConfig)
            else -> false
        }
    }

    fun checkCcData(ccConfig: CreditCardConfigModel?): Boolean {
        if (ccConfig == null) return false
        else {
            if (ccConfig.ccMask == null || ccConfig.ccExpiry == null || ccConfig.ccType == null || ccConfig.ccHolderName == null) return false
        }
        return true
    }

    fun checkSepaData(sepaConfig: SepaConfigModel?): Boolean {
        if (sepaConfig == null) return false
        else {
            if (sepaConfig.iban == null || sepaConfig.bic == null) return false
        }
        return true
    }

    fun checkPaypalData(paypalConfig: PayPalConfigModel?): Boolean {
        if (paypalConfig == null) return false
        else {
            if (paypalConfig.nonce == null || paypalConfig.billingAgreementId == null || paypalConfig.deviceData == null) return false
        }
        return true
    }
}
