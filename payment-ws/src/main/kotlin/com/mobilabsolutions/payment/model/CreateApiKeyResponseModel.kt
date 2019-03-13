package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Creat Api Key Response")
data class CreateApiKeyResponseModel(
    @ApiModelProperty(value = "Api key name")
    val apiKeyName: String?,

    @ApiModelProperty(value = "Key type")
    val apiKeyType: KeyType?
)