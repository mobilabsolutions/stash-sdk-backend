/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.validation.PaymentServiceProviderEnumValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Config Request")
data class PspConfigRequestModel(
    @ApiModelProperty(value = "Payment service provider type", example = "Values: BS_PAYONE, BRAINTREE, ADYEN")
    @field:PaymentServiceProviderEnumValidator(PaymentServiceProvider = PaymentServiceProvider::class)
    @field:NotNull
    val pspId: String?,

    @ApiModelProperty(value = "PSP configuration model")
    val pspConfig: PspUpsertConfigRequestModel
)
