package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
data class ApiKeyReturnInfoModel(
    @ApiModelProperty(value = "Api key id", example = "1")
    val id: Long?,

    @ApiModelProperty(value = "Api key name", example = "Test key")
    val name: String?,

    @ApiModelProperty(value = "Api key type", example = "PUBLISHABLE")
    @field:Enumerated(EnumType.STRING)
    val type: KeyType?,

    @ApiModelProperty(value = "Api key", example = "Mobilab-dkoeheDaXaqnQp")
    val key: String? = null
)
