package com.mobilabsolutions.payment.bspayone.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Sepa Details")
data class BsPayoneSepaDetailsModel(
    @ApiModelProperty(value = "International Bank Account Number", example = "DE00123456782599100004")
    val iban: String,

    @ApiModelProperty(value = "Bank Identifier Code", example = "TESTTEST")
    val bic: String
)