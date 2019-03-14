package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.service.ApiKeyService
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Get All Api Keys Response")
data class GetApiKeyResponseModel(
    @ApiModelProperty(value = "List of api keys")
    val data: MutableList<ApiKeyService.ApiKeyReturnInfo>
)