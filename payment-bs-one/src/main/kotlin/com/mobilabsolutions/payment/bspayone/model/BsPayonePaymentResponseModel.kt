package com.mobilabsolutions.payment.bspayone.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneResponseStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Payment Response")
data class BsPayonePaymentResponseModel(
    @ApiModelProperty(value = "BS Payone response status", example = "APPROVED")
    @field:Enumerated(EnumType.STRING)
    val status: BsPayoneResponseStatus?,

    @ApiModelProperty(value = "BS Payone transaction id")
    @JsonProperty(value = "txid")
    val transactionId: String?,

    @ApiModelProperty(value = "BS Payone customern id")
    @JsonProperty(value = "userid")
    val customerId: String?,

    @ApiModelProperty(value = "BS Payone error code")
    @JsonProperty(value = "errorcode")
    val errorCode: String?,

    @ApiModelProperty(value = "BS Payone error message")
    @JsonProperty(value = "errormessage")
    val errorMessage: String?,

    @ApiModelProperty(value = "BS Payone customer message")
    @JsonProperty(value = "customermessage")
    val customerMessage: String?
)