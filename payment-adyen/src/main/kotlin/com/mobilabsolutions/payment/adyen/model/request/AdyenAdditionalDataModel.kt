/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen additional data model")
data class AdyenAdditionalDataModel(
    @ApiModelProperty(value = "Whether 3D Secure should be executed or not", example = "true")
    val allow3DS2: Boolean?,

    @ApiModelProperty(value = "Whether 3D Secure should be executed or not", example = "true")
    val executeThreeD: String?
)
