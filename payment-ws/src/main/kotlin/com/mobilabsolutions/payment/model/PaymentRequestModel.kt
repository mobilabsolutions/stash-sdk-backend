package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel("Payment Request Model")
data class PaymentRequestModel(
    @ApiModelProperty("Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    @field:NotNull
    val aliasId: String,

    @ApiModelProperty("Payment data")
    @field:Valid
    @field:NotNull
    val paymentData: PaymentDataModel,

    @ApiModelProperty("Purchase ID", example = "132")
    val purchaseId: String?,

    @ApiModelProperty("Customer ID", example = "122")
    val customerId: String?
)