package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Standard Parameters")
data class BsPayoneStandardParametersModel(
    @ApiModelProperty(value = "Merchant ID", example = "42865")
    @JsonProperty(value = "mid")
    val merchantId: String?,

    @ApiModelProperty(value = "Portal ID", example = "2030968")
    @JsonProperty(value = "portalid")
    val portalId: String?,

    @ApiModelProperty(value = "Key", example = "1234567890")
    val key: String,

    @ApiModelProperty(value = "API version", example = "3.11")
    @JsonProperty(value = "api_version")
    val apiVersion: String,

    @ApiModelProperty(value = "Mode", example = "test")
    val mode: String,

    @ApiModelProperty(value = "Request", example = "authorization")
    val request: String,

    @ApiModelProperty(value = "Encoding", example = "UTF-8")
    val encoding: String
)