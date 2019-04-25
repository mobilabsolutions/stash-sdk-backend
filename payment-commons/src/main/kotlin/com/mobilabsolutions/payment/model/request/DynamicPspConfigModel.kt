package com.mobilabsolutions.payment.model.request

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "Dynamic Psp Config Model")
data class DynamicPspConfigModel(
    @ApiModelProperty(value = "Token", example = "eyJaZXZzY2VGaW5nZXJwcmludFZlcnNbb24iOiIxLjAiLCJwbGF0Zm9ybSI6IkFuZHJvaWQiLCJvc1ZlcnNpb24iOiIyNiIsInNka1ZlcnNpb24iOiIyLjQuMyIsImRldmljZU1vZGVsIjoic2Ftc3VuZyBkcmahbTJsdGUiLCJkZXZpY2VJZGVudGlmaWVyIjoiODFlNjhhNWFkOWFkOGRhxiIsImludGVncmF0aW9uIjoiY3VzdG9tIiwibG9jYWxlIjoiZW5fVVMiLCJnZW5lcmF0aW9uVGltZSI6IjIwMTktMDQtMjNUMTM6MDU6MjZaIn0=")
    val token: String?,

    @ApiModelProperty(value = "Return URL", example = "app://")
    val returnUrl: String?,

    @ApiModelProperty(value = "Channel", example = "Android")
    val channel: String?
)
