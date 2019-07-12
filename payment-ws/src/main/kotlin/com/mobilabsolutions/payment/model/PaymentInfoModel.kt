/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Payment Information Model")
data class PaymentInfoModel(
    @ApiModelProperty("Alias extra")
    @field:Valid
    val extra: AliasExtraModel?,

    @ApiModelProperty("Psp configuration")
    @field:Valid
    val pspConfig: PspConfigModel?
)
