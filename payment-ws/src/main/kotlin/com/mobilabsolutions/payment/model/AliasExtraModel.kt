package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.message.CreditCardConfigModel
import com.mobilabsolutions.payment.message.SepaConfigModel
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
    @ApiModelProperty(value = "Payment service provider alias")
    @field:Valid
    val ccConfig: CreditCardConfigModel?,

    @ApiModelProperty(value = "Payment service provider alias")
    @field:Valid
    val sepaConfig: SepaConfigModel?,

    @ApiModelProperty(value = "Payment method", example = "SEPA")
    @field:Enumerated(EnumType.STRING)
    @field:NotNull
    val paymentMethod: PaymentMethod?
)