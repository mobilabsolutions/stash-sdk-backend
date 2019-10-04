/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.bspayone.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone standard parameters model")
data class BsPayoneStandardParametersModel(
    @ApiModelProperty(value = "Merchant ID", example = "42865")
    @JsonProperty(value = "mid")
    @field:NotNull
    val merchantId: String?,

    @ApiModelProperty(value = "Portal ID", example = "2030968")
    @JsonProperty(value = "portalid")
    @field:NotNull
    val portalId: String?,

    @ApiModelProperty(value = "Key", example = "1234567890")
    @field:NotNull
    val key: String?,

    @ApiModelProperty(value = "API version", example = "3.11")
    @JsonProperty(value = "api_version")
    @field:NotNull
    val apiVersion: String?,

    @ApiModelProperty(value = "Mode", example = "test")
    @field:NotNull
    val mode: String?,

    @ApiModelProperty(value = "Request", example = "authorization")
    @field:NotNull
    val request: String?,

    @ApiModelProperty(value = "Encoding", example = "UTF-8")
    @field:NotNull
    val encoding: String?
)
