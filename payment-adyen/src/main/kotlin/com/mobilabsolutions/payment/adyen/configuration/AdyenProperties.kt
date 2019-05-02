package com.mobilabsolutions.payment.adyen.configuration

import org.springframework.context.annotation.Configuration

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Configuration
class AdyenProperties {
    val testPaymentBaseUrl = "https://pal-test.adyen.com/pal/servlet/Payment/v40"
    val testCheckoutBaseUrl = "https://checkout-test.adyen.com/v41"
    val livePaymentBaseUrl = "https://%s-pal-live.adyen.com/pal/servlet/Payment/v40"
    val liveCheckoutBaseUrl = "https://%s-checkout-live.adyen.com/v41"
}
