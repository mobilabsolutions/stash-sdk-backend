package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneResponseStatus
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
data class BsPayoneDeleteAliasResponseModel(
    @ApiModelProperty(value = "BS Payone response status", example = "APPROVED")
    @field:Enumerated(EnumType.STRING)
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