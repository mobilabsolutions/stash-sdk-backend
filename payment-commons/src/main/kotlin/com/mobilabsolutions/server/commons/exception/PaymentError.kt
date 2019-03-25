package com.mobilabsolutions.server.commons.exception

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
enum class PaymentError(val error: String) {
    CC_REGISTRATION_ERROR("Credit card registration failed"),
    SEPA_REGISTRATION_ERROR("Sepa registration failed"),
    PAYMENT_ERROR("Payment execution failed"),
    REFUND_ERROR("Refund failed"),
    TEMPORARY_ERROR("Temporary server error, please try again"),
    UNKNOWN_ERROR("Unknown error occurred, please contact support")
}