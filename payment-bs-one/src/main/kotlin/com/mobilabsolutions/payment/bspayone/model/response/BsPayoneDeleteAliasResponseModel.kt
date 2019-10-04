/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.bspayone.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneResponseStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone delete alias response model")
data class BsPayoneDeleteAliasResponseModel(
    @ApiModelProperty(value = "BS Payone response status", example = "APPROVED")
    val status: BsPayoneResponseStatus?,

    @ApiModelProperty(value = "BS Payone error code")
    @JsonProperty(value = "errorcode")
    val errorCode: String?,

    @ApiModelProperty(value = "BS Payone error message")
    @JsonProperty(value = "errormessage")
    val errorMessage: String?,

    @ApiModelProperty(value = "BS Payone customer message")
    @JsonProperty(value = "customermessage")
    val customerMessage: String?
) {
    fun hasError() = status == BsPayoneResponseStatus.ERROR
}
