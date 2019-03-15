package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Get All Api Keys Response")
data class GetApiKeyResponseModel(
    @ApiModelProperty(value = "List of api keys")
    val data: List<ApiKeyReturnInfoModel>
)