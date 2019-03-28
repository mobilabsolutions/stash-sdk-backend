package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PayPal Configuration")
data class PayPalConfigModel(
    @ApiModelProperty(value = "PayPal nonce", example = "cnbaskjcbjakbjv")
    val nonce: String?,

    @ApiModelProperty(value = "PayPal billing agreement id", example = "123123")
    val billingAgreementId: String?,

    @ApiModelProperty(value = "PayPal device data", example = "{{\"correlation_id\":\"73e463b6abef4de690f3b90c940b54d3\"}")
    val deviceData: String?
)