package com.mobilabsolutions.payment.message

data class PspConfigMessage(
    val type: String?,
    val merchantId: String?,
    val portalId: String?,
    val apiVersion: String?,
    val request: String?,
    val responseType: String?,
    val encoding: String?,
    val hash: String?,
    val accountId: String?,
    val mode: String?
)