package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.message.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Merchant Creation Request")
data class MerchantRequestModel(
    @ApiModelProperty(value = "Merchant id", example = "Mobilab")
    val merchantId: String?,

    @ApiModelProperty(value = "Merchant name", example = "Mobilab's Best Merchant")
    val merchantName: String?,

    @ApiModelProperty(value = "Merchant email", example = "merchant@mobilabsolutions.com")
    val merchantEmail: String?,

    @ApiModelProperty(value = "Merchant psp config")
    val merchantPspConfig: PspConfigModel,

    @ApiModelProperty(value = "Merchant default currency", example = "EUR")
    val merchantCurrency: String?
)