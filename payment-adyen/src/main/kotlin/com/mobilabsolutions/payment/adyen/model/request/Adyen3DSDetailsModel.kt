/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.adyen.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Adyen 3DS details model")
data class Adyen3DSDetailsModel(
    @ApiModelProperty(value = "Fingerprint result from the client", example = "eyJ0aHJlZURTQ29tcEluZCI6ICJZIn0=")
    @JsonProperty(value = "threeds2.fingerprint")
    val fingerprintResult: String?,

    @ApiModelProperty(value = "Challenge result from the client", example = "eyJ0cmFuc1N0YXR1cyI6IlkifQ==")
    @JsonProperty(value = "threeds2.challengeResult")
    val challengeResult: String?,

    @ApiModelProperty(value = "Value received when the shopper was redirected back to your website", example = "djIhMF...")
    @JsonProperty(value = "MD")
    val md: String?,

    @ApiModelProperty(value = "Value received when the shopper was redirected back to your website", example = "eNpVU...")
    @JsonProperty(value = "PaRes")
    val paRes: String?
)
