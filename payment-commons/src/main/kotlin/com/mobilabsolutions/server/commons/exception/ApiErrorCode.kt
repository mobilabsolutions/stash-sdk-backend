package com.mobilabsolutions.server.commons.exception

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
enum class ApiErrorCode(val code: String, val message: String) {
    AUTHENTICATION_ERROR("1000", "Authentication error"),
    INSUFFICIENT_RIGHTS("1001", "Authenticated user doesn't have the required rights for this operation."),
    NO_RIGHTS("1002", "There are no roles defined for given merchant."),
    INCORRECT_OLD_PASSWORD("1003", "Old password for given user is incorrect."),

    VALIDATION_ERROR("2000", "Validation error"),
    CONSTRAINT_VALIDATION_FAILED("2001", "Validation error"),
    MISSING_REQUEST_HEADER("2002", "Validation error"),
    ARGUMENT_NOT_VALID("2003", "Validation error"),
    MESSAGE_NOT_READABLE("2004", "Validation error"),
    MULTIPART_NOT_VALID("2005", "Validation error"),
    ARGUMENT_TYPE_MISMATCH("2006", "Validation error"),

    PUBLISHABLE_KEY_NOT_FOUND("3000", "Publishable Key cannot be found."),
    SECRET_KEY_NOT_FOUND("3001", "Secret Key cannot be found."),
    PSP_CONF_FOR_MERCHANT_EMPTY("3002", "There are no PSP configurations defined for given merchant."),
    PSP_CONF_FOR_MERCHANT_NOT_FOUND("3003", "PSP configuration for given `PSP-Type` cannot be found from given merchant."),
    PSP_IMPL_NOT_FOUND("3004", "PSP implementation for given `PSP-Type` cannot be found."),
    ALIAS_NOT_FOUND("3005", "Alias ID cannot be found."),
    WRONG_ALIAS_MERCHANT_MAPPING("3006", "Alias does not map to correct merchant."),
    INCOMPLETE_ALIAS("3007", "Given alias is incomplete, please define a payment configuration on related alias."),
    MERCHANT_NOT_FOUND("3008", "Given merchant id cannot be found."),
    MERCHANT_API_KEY_EMPTY("3009", "There no API KEYS defined for given merchant."),
    MERCHANT_API_KEY_NOT_FOUND("3010", "Given merchant api key id cannot be found."),
    MERCHANT_ALREADY_EXISTS("3011", "Merchant with given id already exists."),
    TRANSACTION_NOT_FOUND("3012", "Given transaction id cannot be found."),
    TRANSACTION_ALREADY_CAPTURED("3013", "Transaction was already captured, please try the refund instead."),
    PSP_TEST_MODE_INCONSISTENT("3014", "`PSP-Test-Mode` for this transaction is different than the mode for previous transaction."),
    ONLY_PAYPAL_ALLOWED("3015", "Only PayPal registration is allowed for given PSP type."),
    TRANSACTION_NOT_ALLOWED("3016", "Transaction operation is not allowed."),
    SEPA_NOT_ALLOWED("3017", "SEPA is not supported for this operation."),

    PSP_MODULE_ERROR("4000", "Unexpected PSP operation error.")
}
