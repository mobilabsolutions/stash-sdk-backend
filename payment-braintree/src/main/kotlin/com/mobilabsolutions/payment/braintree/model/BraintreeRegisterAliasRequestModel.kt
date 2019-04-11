package com.mobilabsolutions.payment.braintree.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Braintree Register Alias Request")
data class BraintreeRegisterAliasRequestModel(
    @ApiModelProperty(value = "Customer ID", example = "123456")
    val customerId: String?,

    @ApiModelProperty(value = "PayPal nonce", example = "cnbaskjcbjakbjv")
    val nonce: String?,

    @ApiModelProperty(value = "PayPal device data", example = "{\"correlation_id\":\"73e463b6abef4de690f3b90c940b54d3\"}")
    val deviceData: String?
)
