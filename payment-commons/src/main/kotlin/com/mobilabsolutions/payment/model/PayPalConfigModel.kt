package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PayPal Configuration")
data class PayPalConfigModel(
    @ApiModelProperty(value = "PayPal nonce", example = "cnbaskjcbjakbjv")
    val nonce: String,

    @ApiModelProperty(value = "PayPal billing agreement id", example = "123123")
    val billingAgreementId: String
)