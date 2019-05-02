package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Adyen Credentials Request")
data class AdyenCredentialsRequestModel(
    @ApiModelProperty(value = "Username", example = "ws@company.yourcompany")
    val username: String?,

    @ApiModelProperty(value = "Password", example = "mypassword123")
    val password: String?,

    @ApiModelProperty(value = "Live URL prefix", example = "[random]-[company-name]")
    val urlPrefix: String?
)
