/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "API key model")
data class ApiKeyInfoModel(
    @ApiModelProperty(value = "Api key id", example = "1")
    val id: Long?,

    @ApiModelProperty(value = "Api key name", example = "Test key")
    val name: String?,

    @ApiModelProperty(value = "Api key type", example = "Values: PUBLISHABLE, SECRET")
    val type: KeyType?,

    @ApiModelProperty(value = "Api key", example = "Mobilab-dkoeheDaXaqnQp")
    val key: String? = null
)
