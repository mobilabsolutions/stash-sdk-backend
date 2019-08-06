/*
 * Copyright © MobiLab Solutions GmbH
 */

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
        return when (aliasModel?.paymentMethod) {
            PaymentMethod.CC.name -> checkCcData(aliasModel, pspType)
            PaymentMethod.SEPA.name -> checkSepaData(aliasModel, pspType)
            PaymentMethod.PAY_PAL.name -> checkPaypalData(aliasModel, pspType)
            PaymentMethod.THREE_D_SECURE.name -> true
            else -> false
        }
    }

    private fun checkCcData(aliasExtra: AliasExtraModel, pspType: String): Boolean {
        return when (pspType) {
            PaymentServiceProvider.BS_PAYONE.name -> {
                (aliasExtra.personalData?.country != null && aliasExtra.personalData.firstName != null && aliasExtra.personalData.lastName != null && checkCcConfig(aliasExtra.ccConfig))
            }
            PaymentServiceProvider.ADYEN.name -> {
                (aliasExtra.personalData?.firstName != null && aliasExtra.personalData.lastName != null && checkCcConfig(aliasExtra.ccConfig))
            }
            PaymentServiceProvider.BRAINTREE.name -> {
                ((aliasExtra.ccConfig?.nonce != null && aliasExtra.ccConfig.deviceData != null))
            }
            else -> false
        }
    }

    private fun checkSepaData(aliasExtra: AliasExtraModel, pspType: String): Boolean {
        return when (pspType) {
            PaymentServiceProvider.BS_PAYONE.name -> {
                (aliasExtra.personalData?.country != null && aliasExtra.personalData.firstName != null && aliasExtra.personalData.lastName != null && checkSepaConfig(aliasExtra.sepaConfig, pspType))
            }
            PaymentServiceProvider.ADYEN.name -> {
                (aliasExtra.personalData?.firstName != null && aliasExtra.personalData.lastName != null && checkSepaConfig(aliasExtra.sepaConfig, pspType))
            }
            else -> false
        }
    }

    private fun checkPaypalData(aliasExtra: AliasExtraModel, pspType: String): Boolean {
        return when (pspType) {
            PaymentServiceProvider.BRAINTREE.name -> {
                (aliasExtra.payPalConfig?.nonce != null && aliasExtra.payPalConfig.deviceData != null && aliasExtra.personalData?.email != null)
            }
            else -> false
        }
    }

    private fun checkCcConfig(ccConfig: CreditCardConfigModel?): Boolean {
        return when (ccConfig) {
            null -> false
            else -> (ccConfig.ccMask != null && ccConfig.ccExpiry != null && ccConfig.ccType != null && ccConfig.ccHolderName != null)
        }
    }

    private fun checkSepaConfig(sepaConfig: SepaConfigModel?, pspType: String): Boolean {
        return when (sepaConfig) {
            null -> false
            else -> {
                return when (pspType) {
                    PaymentServiceProvider.BS_PAYONE.name -> (sepaConfig.iban != null)
                    PaymentServiceProvider.ADYEN.name -> (sepaConfig.iban != null)
                    else -> false
                }
            }
        }
    }
}
