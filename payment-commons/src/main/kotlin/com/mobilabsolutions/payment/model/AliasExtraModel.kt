package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "Alias Extra")
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

    @ApiModelProperty(value = "Personal data")
    val personalData: PersonalDataModel?,

    @ApiModelProperty(value = "Payment method", example = "SEPA")
    @field:Enumerated(EnumType.STRING)
    @field:NotNull
    val paymentMethod: PaymentMethod?
)