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

    @ApiModelProperty(value = "Fingerprint result from the client", example = "eyJ0aHJlZURTQ29tcEluZCI6ICJZIn0=")
    val fingerprintResult: String?,

    @ApiModelProperty(value = "Challenge result from the client", example = "eyJ0cmFuc1N0YXR1cyI6IlkifQ==")
    val challengeResult: String?
)
