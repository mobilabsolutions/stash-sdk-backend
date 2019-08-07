package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Verify alias request model")
data class VerifyAliasRequestModel(
    @ApiModelProperty(value = "Fingerprint result from the client", example = "eyJ0aHJlZURTQ29tcEluZCI6ICJZIn0=")
    val fingerprintResult: String?
)
