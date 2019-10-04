/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
data class WebhookCredentialsRequestModel(
    @ApiModelProperty("Webhook URL", example = "https://test.mblb.net/notifications")
    @field:NotNull
    val webhookUrl: String?,

    @ApiModelProperty("Webhook username", example = "username")
    @field:NotNull
    val webhookUsername: String?,

    @ApiModelProperty("Webhook password", example = "password")
    @field:NotNull
    val webhookPassword: String?
)
