package com.mobilabsolutions.payment.data.enum

import io.swagger.annotations.ApiModel

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Payment methods: ")
enum class PaymentMethod {
    CC,
    SEPA,
    PAY_PAL,
    GOOGLE_PAY,
    APPLE_PAY,
    KLARNA
}
