package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Api Key Request")
data class ApiKeyRequestModel(
    @ApiModelProperty(value = "Api key type", example = "Publishable")
    @field:Enumerated(EnumType.STRING)
    val type: KeyType?,

    @ApiModelProperty(value = "Api key name", example = "Test key")
    val name: String?
)