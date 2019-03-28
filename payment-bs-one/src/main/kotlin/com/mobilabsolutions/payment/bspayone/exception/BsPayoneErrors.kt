package com.mobilabsolutions.payment.bspayone.exception

import com.mobilabsolutions.server.commons.exception.PaymentError

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
enum class BsPayoneErrors(val code: String, val error: PaymentError) {

    LIMIT_EXCEEDED("13", PaymentError.PAYMENT_ERROR),
    CARD_STOLEN("43", PaymentError.PAYMENT_ERROR),
    CARD_UNKNOWN("56", PaymentError.PAYMENT_ERROR),
    CARD_CANCELED("62", PaymentError.PAYMENT_ERROR),
    BIC_COUNTRY_NOT_SUPPORTED("889", PaymentError.PAYMENT_ERROR),
    FRAUD_DETECTION("107", PaymentError.PAYMENT_ERROR),
    AMOUNT_TOO_LOW("301", PaymentError.PAYMENT_ERROR),
    BIN_CHECK_DECLINED("701", PaymentError.PAYMENT_ERROR),
    BIN_COUNTRY_DECLINED("702", PaymentError.PAYMENT_ERROR),
    IP_CHECK_DECLINED("703", PaymentError.PAYMENT_ERROR),
    IP_COUNTRY_DECLINED("704", PaymentError.PAYMENT_ERROR),
    POS_CHECK_DECLINED("710", PaymentError.PAYMENT_ERROR),
    VELOCITY_IP_CHECK_DECLINED("721", PaymentError.PAYMENT_ERROR),
    VELOCITY_CARD_NUMBER_CHECK_DECLINED("722", PaymentError.PAYMENT_ERROR),
    VELOCITY_ACCOUNT_NUMBER_CHECK_DECLINED("723", PaymentError.PAYMENT_ERROR),
    VELOCITY_EMAIL_CHECK_DECLINED("724", PaymentError.PAYMENT_ERROR),
    BLACKLIST_IP_CHECK_REJECTED("731", PaymentError.PAYMENT_ERROR),
    BLACKLIST_CARDPAN_CHECK_REJECTED("732", PaymentError.PAYMENT_ERROR),
    BLACKLIST_BANK_ACCOUNT_CHECK_REJECTED("733", PaymentError.PAYMENT_ERROR),
    BLACKLIST_EMAIL_CHECK_REJECTED("734", PaymentError.PAYMENT_ERROR),
    INVALID_BIC("887", PaymentError.PAYMENT_ERROR),
    INVALID_IBAN("888", PaymentError.PAYMENT_ERROR),
    CARD_MISMATCH("880", PaymentError.PAYMENT_ERROR),
    FRAUD_DETECTION_2("890", PaymentError.PAYMENT_ERROR),
    DEBTOR_LIMIT_EXCEEDED("891", PaymentError.PAYMENT_ERROR),
    PAYMENT_TYPE_MISMATCH("923", PaymentError.PAYMENT_ERROR),
    AMOUNT_TOO_SMALL("944", PaymentError.PAYMENT_ERROR),
    AMOUNT_TOO_HIGH("945", PaymentError.PAYMENT_ERROR),
    AMOUNT_TOO_BIG_OR_SMALL("962", PaymentError.PAYMENT_ERROR),
    ALIAS_WRONG_OR_MISSING("1073", PaymentError.PAYMENT_ERROR),

    REFUND_LIMIT_EXCEEDED("917", PaymentError.REFUND_ERROR),

    CARD_ISSUER_NOT_AVAILABLE("1", PaymentError.TEMPORARY_ERROR),
    CARD_ISSUER_NOT_AVAILABLE_2("91", PaymentError.TEMPORARY_ERROR),
    DB_CONNECTION_FAILURE("909", PaymentError.TEMPORARY_ERROR),
    STATUS_CHANGE_NOT_POSSIBLE("950", PaymentError.TEMPORARY_ERROR),
    MAINTENANCE("990", PaymentError.TEMPORARY_ERROR),
    MAINTENANCE_2("991", PaymentError.TEMPORARY_ERROR),
    SERVICE_UNAVAILABLE("6502", PaymentError.TEMPORARY_ERROR);

    companion object {
        fun mapResponseCode(responseCode: String): PaymentError {
            return values().firstOrNull { it.code == responseCode }?.error ?: PaymentError.UNKNOWN_ERROR
        }
    }
}