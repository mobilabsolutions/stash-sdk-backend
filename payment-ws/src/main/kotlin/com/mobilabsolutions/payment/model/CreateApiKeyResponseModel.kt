package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Creat Api Key Response")
data class CreateApiKeyResponseModel(
    @ApiModelProperty(value = "Api key name", example = "Test key")
    val name: String?,

    @ApiModelProperty(value = "Key type", example = "PUBLIC")
    @field:Enumerated(EnumType.STRING)
    val type: KeyType?
)