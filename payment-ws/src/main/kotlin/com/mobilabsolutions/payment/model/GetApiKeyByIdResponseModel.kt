package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType
import io.swagger.annotations.ApiModel

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Get Api Key By Id Response")
data class GetApiKeyByIdResponseModel(
    val apiKeyId: Long?,
    val name: String?,
    val apiKeyType: KeyType?
)