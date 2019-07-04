package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "3D Secure Configuration")
data class ThreeDSecureConfigModel(
    @ApiModelProperty(value = "Payload needed to verify the payment", example = "Ab02b4c0!BQABAgCYHYurjVnu8GRyhy1ZsGj...")
    val paymentData: String?,

    @ApiModelProperty(value = "Payload needed to verify the payment", example = "VkVneDFKL3NGbFlNZ05QM0VzeXZKQT09ISuR...")
    val md: String?,

    @ApiModelProperty(value = "Response received when redirecting the shopper", example = "eNpVUl1zgjAQ/CvWH0ASkA+ZMz...")
    val paRes: String?
)
