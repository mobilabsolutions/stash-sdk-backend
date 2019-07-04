package com.mobilabsolutions.payment.model.response

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Exchange Alias Response")
data class ExchangeAliasResponseModel(
    @ApiModelProperty(value = "Payload needed when redirecting the shopper", example = "eNpVUl1zgjAQ/CvWH0ASkA+ZMzNUO...")
    val paReq: String?,

    @ApiModelProperty(value = "The returnUrl provided in the request", example = "https://payment-dev.mblb.net")
    val termUrl: String?,

    @ApiModelProperty(value = "Payload needed to verify the payment", example = "VkVneDFKL3NGbFlNZ05QM0VzeXZKQT09ISuR...")
    val md: String?,

    @ApiModelProperty(value = "URL to where the shopper should be redirected to complete a 3D Secure authentication", example = "https://test.adyen.com/hpp/3d/validate.shtml")
    val url: String?
)
