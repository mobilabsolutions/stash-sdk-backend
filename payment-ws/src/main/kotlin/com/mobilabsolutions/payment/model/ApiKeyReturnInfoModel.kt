package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
data class ApiKeyReturnInfoModel(
    @ApiModelProperty(value = "Api key id", example = "1")
    val id: Long?,

    @ApiModelProperty(value = "Api key name", example = "Test key")
    val name: String?,

    @ApiModelProperty(value = "Api key type", example = "PUBLIC")
    val type: KeyType?
)