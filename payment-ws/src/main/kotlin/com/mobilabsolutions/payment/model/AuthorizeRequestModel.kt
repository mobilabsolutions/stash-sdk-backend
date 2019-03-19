package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Authorize Request Model")
data class AuthorizeRequestModel(
    @ApiModelProperty("Alias ID")
    val aliasId: String?,

    @ApiModelProperty("Payment data")
    @field:NotNull
    val paymentData: PaymentDataModel,

    @ApiModelProperty("Purchase ID")
    val purchaseId: String?,

    @ApiModelProperty("Customer ID")
    val customerId: String
)