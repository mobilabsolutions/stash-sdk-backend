package com.mobilabsolutions.payment.adyen.data.enum

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
enum class AdyenResultCode(val result: String) {
    AUTHORISED("Authorised"),
    REFUSED("Refused"),
    REDIRECT_SHOPPER("RedirectShopper"),
    RECEIVED("Received"),
    CANCELLED("Cancelled"),
    PENDING("Pending"),
    ERROR("Error")
}
