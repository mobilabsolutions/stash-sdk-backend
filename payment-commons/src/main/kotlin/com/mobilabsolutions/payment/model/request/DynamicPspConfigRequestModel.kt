/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.data.enum.Channel
import com.mobilabsolutions.payment.validation.ChannelValidator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Dynamic psp config request model")
data class DynamicPspConfigRequestModel(
    @ApiModelProperty(value = "Token", example = "eyJaZXZzY2VGaW5nZXJwcmludFZlcnNbb24iOiIxLjAiLCJwbGF0Zm9ybSI6IkFuZHJvaWQiLCJvc1ZlcnNpb24iOiIyNiIsInNka1ZlcnNpb24iOiIyLjQuMyIsImRldmljZU1vZGVsIjoic2Ftc3VuZyBkcmahbTJsdGUiLCJkZXZpY2VJZGVudGlmaWVyIjoiODFlNjhhNWFkOWFkOGRhxiIsImludGVncmF0aW9uIjoiY3VzdG9tIiwibG9jYWxlIjoiZW5fVVMiLCJnZW5lcmF0aW9uVGltZSI6IjIwMTktMDQtMjNUMTM6MDU6MjZaIn0=")
    val token: String?,

    @ApiModelProperty(value = "Return URL", example = "app://")
    val returnUrl: String?,

    @ApiModelProperty(value = "Channel", example = "Android")
    @field:ChannelValidator(Channel = Channel::class)
    val channel: String?
)
