package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Config Request")
data class PspConfigRequestModel(
    @ApiModelProperty(value = "Payment service provider type", example = "BS_PAYONE")
    @field:Enumerated(EnumType.STRING)
    val pspId: PaymentServiceProvider,

    @ApiModelProperty(value = "PSP configuration model")
    val pspConfig: PspUpsertConfigRequestModel
)
