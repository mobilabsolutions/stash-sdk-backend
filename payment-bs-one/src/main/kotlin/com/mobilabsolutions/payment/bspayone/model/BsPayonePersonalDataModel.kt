package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Personal Data")
data class BsPayonePersonalDataModel(
    @ApiModelProperty(value = "Last name", example = "Mustermann")
    @JsonProperty(value = "lastname")
    val lastName: String,

    @ApiModelProperty(value = "Country", example = "Germany")
    val country: String,

    @ApiModelProperty(value = "City", example = "Cologne")
    val city: String
)