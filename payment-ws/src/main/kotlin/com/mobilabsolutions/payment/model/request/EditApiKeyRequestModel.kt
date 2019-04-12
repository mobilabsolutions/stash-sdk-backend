package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel("Edit api key request model")
data class EditApiKeyRequestModel(
    @ApiModelProperty(value = "Api key name", example = "Test key")
    val name: String?
)
