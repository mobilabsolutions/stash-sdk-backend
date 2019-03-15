package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.KeyType

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
data class ApiKeyReturnInfoModel(val merchantId: String, val apiKeyName: String?, val apiKeyType: KeyType?)