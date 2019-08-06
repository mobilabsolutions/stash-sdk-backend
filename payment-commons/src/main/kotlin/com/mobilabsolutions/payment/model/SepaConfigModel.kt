/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "SEPA config model")
data class SepaConfigModel(
    @ApiModelProperty(value = "International Bank Account Number", example = "DE00123456782599100004")
    val iban: String?,

    @ApiModelProperty(value = "Bank Identifier Code", example = "TESTTEST")
    val bic: String?
)
