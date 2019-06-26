package com.mobilabsolutions.payment.adyen.configuration

import org.json.JSONException
import org.json.JSONObject
import org.springframework.context.annotation.Configuration

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Configuration
class AdyenProperties {
    val testPaymentBaseUrl = "https://pal-test.adyen.com/pal/servlet/Payment/v46"
    val testCheckoutBaseUrl = "https://checkout-test.adyen.com/v49"
    val testRecurringBaseUrl = "https://pal-test.adyen.com/pal/servlet/Recurring/v30"
    val livePaymentBaseUrl = "https://%s-pal-live.adyen.com/pal/servlet/Payment/v46"
    val liveCheckoutBaseUrl = "https://%s-checkout-live.adyen.com/v49"
    val liveRecurringBaseUrl = "https://%s-pal-live.adyen.com/pal/servlet/Recurring/v30"
    val contract = "RECURRING"
    val shopperInteraction = "ContAuth"
    val sepaPaymentMethod = "sepadirectdebit"
    val threeDSecure = "scheme"
}

internal fun JSONObject.getStringSafe(key: String): String? {
    return try { this.getString(key) } catch (e: JSONException) { null }
}

internal fun JSONObject.getJsonObjectSafe(key: String): JSONObject? {
    return try { this.getJSONObject(key) } catch (e: JSONException) { null }
}
