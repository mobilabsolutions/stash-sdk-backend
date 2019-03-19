package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Configuration")
class PspConfigModel(
    @ApiModelProperty(value = "Payment service provider type", example = "BS_PAYONE")
    val type: String,

    @ApiModelProperty(value = "Merchant ID", example = "42865")
    val merchantId: String?,

    @ApiModelProperty(value = "Portal ID", example = "2030968")
    val portalId: String?,

    @ApiModelProperty(value = "Key", example = "1234567890")
    val key: String?,

    @ApiModelProperty(value = "Account ID", example = "42949")
    val accountId: String?,

    @ApiModelProperty(value = "Public key")
    val publicKey: String?,

    @ApiModelProperty(value = "Private key")
    val privateKey: String?
)