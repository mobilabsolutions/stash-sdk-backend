/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.validation.KeyTypeEnumValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Api Key Request")
data class ApiKeyRequestModel(
    @ApiModelProperty(value = "Api key type", example = "Values: PUBLISHABLE, SECRET")
    @field:KeyTypeEnumValidator(KeyType = KeyType::class)
    @field:NotNull
    val type: String?,

    @ApiModelProperty(value = "Api key name", example = "Test key")
    val name: String?
)
