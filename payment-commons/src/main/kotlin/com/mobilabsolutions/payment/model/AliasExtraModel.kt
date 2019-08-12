/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.validation.PaymentMethodEnumValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Alias extra model")
data class AliasExtraModel(
    @ApiModelProperty(value = "Credit card configuration")
    @field:Valid
    val ccConfig: CreditCardConfigModel?,

    @ApiModelProperty(value = "Sepa configuration")
    @field:Valid
    val sepaConfig: SepaConfigModel?,

    @ApiModelProperty(value = "PayPal configuration")
    @field:Valid
    val payPalConfig: PayPalConfigModel?,

    @ApiModelProperty(value = "3D Secure configuration")
    @field:Valid
    val threeDSecureConfig: ThreeDSecureConfigModel?,

    @ApiModelProperty(value = "Personal data")
    @field:Valid
    val personalData: PersonalDataModel?,

    @ApiModelProperty(value = "Payment method", example = "SEPA")
    @field:PaymentMethodEnumValidator(PaymentMethod = PaymentMethod::class)
    @field:NotNull
    val paymentMethod: String?,

    @ApiModelProperty(value = "Used platform", example = "iOS, Android")
    val channel: String?
)
