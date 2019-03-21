package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.message.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Payment Information Model")
data class PaymentInfoModel(
    @ApiModelProperty("Alias extra")
    val extra: AliasExtraModel?,

    @ApiModelProperty("Psp configuration")
    val pspConfig: PspConfigModel?

)