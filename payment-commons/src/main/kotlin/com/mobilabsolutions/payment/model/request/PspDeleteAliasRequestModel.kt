/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.validation.PaymentMethodEnumValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP DeleteAliasRequest")
data class PspDeleteAliasRequestModel(
    @ApiModelProperty(value = "Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    val aliasId: String?,

    @ApiModelProperty(value = "PSP Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    val pspAlias: String?,

    @ApiModelProperty(value = "Payment method", example = "Values: CC, SEPA, PAY_PAL, GOOGLE_PAY, APPLE_PAY, KLARNA")
    @PaymentMethodEnumValidator(PaymentMethod = PaymentMethod::class)
    val paymentMethod: String?,

    @ApiModelProperty(value = "PSP config")
    val pspConfig: PspConfigModel?,

    @ApiModelProperty(value = "Customer reference", example = "oIXHpTAfEPSleWXT6Khe")
    val customerReference: String?
)
