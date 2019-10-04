/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Verify alias request model")
data class VerifyAliasRequestModel(
    @ApiModelProperty(value = "Fingerprint result from the client", example = "eyJ0aHJlZURTQ29tcEluZCI6ICJZIn0=")
    val fingerprintResult: String?,

    @ApiModelProperty(value = "Challenge result from the client", example = "eyJ0aHJlZURTQ29tcEluZCI6ICJZIn0=")
    val challengeResult: String?,

    @ApiModelProperty(value = "Value received when the shopper was redirected back to your website", example = "djIhMF...")
    val md: String?,

    @ApiModelProperty(value = "Value received when the shopper was redirected back to your website", example = "eNpVU...")
    val paRes: String?
)
