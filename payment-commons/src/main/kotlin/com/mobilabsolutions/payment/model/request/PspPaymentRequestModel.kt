package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.validation.CountryCode
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.Valid

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Payment Request Model")
data class PspPaymentRequestModel(
    @ApiModelProperty("Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    val aliasId: String?,

    @ApiModelProperty("Payment data")
    @field:Valid
    val paymentData: PaymentDataModel?,

    @ApiModelProperty(value = "Last name of user", example = "Mustermann")
    val lastName: String?,

    @ApiModelProperty(value = "City of account holder", example = "Cologne")
    val city: String?,

    @ApiModelProperty(value = "Country code of account holder", example = "DE")
    @field:CountryCode
    val country: String?,

    @ApiModelProperty(value = "Payment method", example = "SEPA")
    @field:Enumerated(EnumType.STRING)
    val paymentMethod: PaymentMethod?,

    @ApiModelProperty(value = "International Bank Account Number", example = "DE00123456782599100004")
    val iban: String?,

    @ApiModelProperty(value = "Bank Identifier Code", example = "TESTTEST")
    val bic: String?,

    @ApiModelProperty(value = "PSP alias", example = "jsklcmn")
    val pspAlias: String?,

    @ApiModelProperty(value = "PSP config")
    val pspConfig: PspConfigModel?
)
