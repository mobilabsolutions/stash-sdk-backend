package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP DeleteAliasRequest")
data class PspDeleteAliasRequestModel(
    @ApiModelProperty(value = "Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    val aliasId: String?,

    @ApiModelProperty(value = "PSP Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    val pspAlias: String?,

    @ApiModelProperty(value = "Payment method", example = "SEPA")
    @field:Enumerated(EnumType.STRING)
    val paymentMethod: PaymentMethod?,

    @ApiModelProperty(value = "PSP config")
    val pspConfig: PspConfigModel?,

    @ApiModelProperty(value = "Alias extra")
    val aliasExtra: AliasExtraModel?
)
