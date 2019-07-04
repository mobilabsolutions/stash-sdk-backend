package com.mobilabsolutions.payment.adyen.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen 3D Secure Details")
data class Adyen3DSecureDetailsModel(
    @ApiModelProperty(value = "Payload needed to verify the payment", example = "VkVneDFKL3NGbFlNZ05QM0VzeXZKQT09ISuR...")
    @JsonProperty(value = "MD")
    val md: String?,

    @ApiModelProperty(value = "Response received when redirecting the shopper", example = "eNpVUl1zgjAQ/CvWH0ASkA+ZMz...")
    @JsonProperty(value = "PaRes")
    val paRes: String?
)
