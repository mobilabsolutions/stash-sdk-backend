package com.mobilabsolutions.payment.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PspConfigMessage(
    val type: String?,
    val merchantId: String?,
    val portalId: String?,
    val apiVersion: String?,
    val request: String?,
    val responseType: String?,
    val encoding: String?,
    val hash: String?,
    val accountId: String?
)