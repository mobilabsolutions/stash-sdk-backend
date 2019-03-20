package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Credit Card Alias")
class BsPayoneCreditCardAliasModel(
    @ApiModelProperty(value = "BS Payone credit card alias")
    @JsonProperty(value = "pseudocardpan")
    val pspAlias: String
)