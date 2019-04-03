package com.mobilabsolutions.server.commons.exception

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
enum class PaymentError(val error: String) {
    PAYMENT_ERROR("Payment execution failed"),
    REFUND_ERROR("Refund failed"),
    CONFIGURATION_ERROR("Invalid PSP configuration, please check again"),
    TEMPORARY_ERROR("Temporary server error, please try again"),
    UNKNOWN_ERROR("Unknown error occurred, please contact support")
}
