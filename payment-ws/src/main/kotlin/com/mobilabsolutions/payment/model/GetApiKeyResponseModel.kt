package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.service.ApiKeyService
import io.swagger.annotations.ApiModel

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Get All Api Keys Response")
data class GetApiKeyResponseModel(
    val data: MutableList<ApiKeyService.ApiKeyReturnInfo>
)