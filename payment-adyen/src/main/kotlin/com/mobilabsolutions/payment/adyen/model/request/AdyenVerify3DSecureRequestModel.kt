package com.mobilabsolutions.payment.adyen.model.request

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
data class AdyenVerify3DSecureRequestModel(
    val paymentData: String?,

    @JsonProperty(value = "details.MD")
    val md: String?,

    @JsonProperty(value = "details.PaRes")
    val paRes: String?
)
