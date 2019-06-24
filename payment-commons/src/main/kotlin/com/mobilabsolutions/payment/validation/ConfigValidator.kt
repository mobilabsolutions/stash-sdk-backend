package com.mobilabsolutions.payment.validation

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.CreditCardConfigModel
import com.mobilabsolutions.payment.model.SepaConfigModel
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Component
class ConfigValidator {
    fun validate(aliasModel: AliasExtraModel?, pspType: String): Boolean {
        return when (aliasModel!!.paymentMethod) {
            PaymentMethod.CC.name -> checkCcData(aliasModel, pspType)
            PaymentMethod.SEPA.name -> checkSepaData(aliasModel, pspType)
            PaymentMethod.PAY_PAL.name -> checkPaypalData(aliasModel, pspType)
            else -> false
        }
    }

    fun checkCcData(aliasExtra: AliasExtraModel, pspType: String): Boolean {
        if (pspType == PaymentServiceProvider.BS_PAYONE.name) {
            if (aliasExtra.personalData?.country == null || aliasExtra.personalData.firstName == null || aliasExtra.personalData.lastName == null || !checkCcConfig(aliasExtra.ccConfig)) return false
            return true
        } else if (pspType == PaymentServiceProvider.ADYEN.name) {
            if (aliasExtra.personalData?.firstName == null || aliasExtra.personalData.lastName == null || !checkCcConfig(aliasExtra.ccConfig)) return false
            return true
        }
        return false
    }

    fun checkSepaData(aliasExtra: AliasExtraModel, pspType: String): Boolean {
        if (pspType == PaymentServiceProvider.BS_PAYONE.name) {
            if (aliasExtra.personalData?.country == null || aliasExtra.personalData.firstName == null || aliasExtra.personalData.lastName == null || !checkSepaConfig(aliasExtra.sepaConfig)) return false
            return true
        } else if (pspType == PaymentServiceProvider.ADYEN.name) {
            if (aliasExtra.personalData?.firstName == null || aliasExtra.personalData.lastName == null || aliasExtra.sepaConfig?.iban == null) return false
            return true
        }
        return false
    }

    fun checkPaypalData(aliasExtra: AliasExtraModel, pspType: String): Boolean {
        if (aliasExtra.payPalConfig == null || pspType != PaymentServiceProvider.BRAINTREE.name) return false
        else {
            if (aliasExtra.payPalConfig.nonce == null || aliasExtra.payPalConfig.deviceData == null || aliasExtra.personalData?.email == null) return false
        }
        return true
    }

    fun checkCcConfig(ccConfig: CreditCardConfigModel?): Boolean {
        if (ccConfig == null) return false
        else {
            if (ccConfig.ccMask == null || ccConfig.ccExpiry == null || ccConfig.ccType == null || ccConfig.ccHolderName == null) return false
        }
        return true
    }

    fun checkSepaConfig(sepaConfig: SepaConfigModel?): Boolean {
        if (sepaConfig == null) return false
        else {
            if (sepaConfig.iban == null || sepaConfig.bic == null) return false
        }
        return true
    }
}
