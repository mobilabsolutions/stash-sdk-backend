/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Creat Api Key Response")
data class ApiKeyResponseModel(
    @ApiModelProperty(value = "Api ID", example = "1")
    val id: Long?,

    @ApiModelProperty(value = "Api key", example = "Mobilab-dkoeheDaXaqnQp")
    val key: String?
)
