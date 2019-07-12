/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.response

import com.mobilabsolutions.payment.model.ApiKeyInfoModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Get All Api Keys Response")
data class ApiKeyListResponseModel(
    @ApiModelProperty(value = "List of api keys")
    val data: List<ApiKeyInfoModel>
)
