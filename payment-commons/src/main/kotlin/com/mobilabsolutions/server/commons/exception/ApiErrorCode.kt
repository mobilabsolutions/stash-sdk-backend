package com.mobilabsolutions.server.commons.exception

import org.springframework.http.HttpStatus

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
enum class ApiErrorCode(val code: String, val message: String, val httpStatus: HttpStatus) {
    AUTHENTICATION_ERROR("1000", "Authentication error", HttpStatus.UNAUTHORIZED),
    INSUFFICIENT_RIGHTS("1001", "Authenticated user doesn't have the required rights for this operation.", HttpStatus.FORBIDDEN),
    NO_RIGHTS("1002", "There are no roles defined for given merchant.", HttpStatus.BAD_REQUEST),
    INCORRECT_OLD_PASSWORD("1003", "Old password for given user is incorrect.", HttpStatus.BAD_REQUEST),
    MERCHANT_USER_ALREADY_EXISTS("1004", "Merchant user with given id already exists.", HttpStatus.BAD_REQUEST),

    VALIDATION_ERROR("2000", "Validation error", HttpStatus.BAD_REQUEST),
    CONSTRAINT_VALIDATION_FAILED("2001", "Validation error", HttpStatus.BAD_REQUEST),
    MISSING_REQUEST_HEADER("2002", "Validation error", HttpStatus.BAD_REQUEST),
    ARGUMENT_NOT_VALID("2003", "Validation error", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_READABLE("2004", "Validation error", HttpStatus.BAD_REQUEST),
    MULTIPART_NOT_VALID("2005", "Validation error", HttpStatus.BAD_REQUEST),
    ARGUMENT_TYPE_MISMATCH("2006", "Validation error", HttpStatus.BAD_REQUEST),

    PUBLISHABLE_KEY_NOT_FOUND("3000", "Publishable Key cannot be found.", HttpStatus.BAD_REQUEST),
    SECRET_KEY_NOT_FOUND("3001", "Secret Key cannot be found.", HttpStatus.BAD_REQUEST),
    PSP_CONF_FOR_MERCHANT_EMPTY("3002", "There are no PSP configurations defined for given merchant.", HttpStatus.BAD_REQUEST),
    PSP_CONF_FOR_MERCHANT_NOT_FOUND("3003", "PSP configuration for given `PSP-Type` cannot be found from given merchant.", HttpStatus.BAD_REQUEST),
    PSP_IMPL_NOT_FOUND("3004", "PSP implementation for the given `PSP-Type` cannot be found.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALIAS_NOT_FOUND("3005", "Alias ID cannot be found.", HttpStatus.BAD_REQUEST),
    WRONG_ALIAS_MERCHANT_MAPPING("3006", "Alias does not map to correct merchant.", HttpStatus.BAD_REQUEST),
    INCOMPLETE_ALIAS("3007", "Given alias is incomplete, please define a payment configuration on related alias.", HttpStatus.BAD_REQUEST),
    MERCHANT_NOT_FOUND("3008", "Given merchant id cannot be found.", HttpStatus.BAD_REQUEST),
    MERCHANT_API_KEY_EMPTY("3009", "There no API KEYS defined for given merchant.", HttpStatus.BAD_REQUEST),
    MERCHANT_API_KEY_NOT_FOUND("3010", "Given merchant API key id cannot be found.", HttpStatus.BAD_REQUEST),
    MERCHANT_ALREADY_EXISTS("3011", "Merchant with given id already exists.", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND("3012", "Given transaction id cannot be found.", HttpStatus.BAD_REQUEST),
    TRANSACTION_ALREADY_CAPTURED("3013", "Transaction was already captured, please try the refund instead.", HttpStatus.BAD_REQUEST),
    PSP_TEST_MODE_INCONSISTENT("3014", "`PSP-Test-Mode` for this transaction is different than the mode for previous transaction.", HttpStatus.BAD_REQUEST),
    ONLY_PAYPAL_ALLOWED("3015", "Only PayPal registration is allowed for given PSP type.", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_ALLOWED("3016", "Transaction operation is not allowed.", HttpStatus.BAD_REQUEST),
    SEPA_NOT_ALLOWED("3017", "SEPA is not supported for this operation.", HttpStatus.BAD_REQUEST),
    INCORRECT_REFUND_VALUE("3018", "Total refund amount is greater than the original payment's amount.", HttpStatus.BAD_REQUEST),
    TRANSACTIONS_NOT_FOUND("3019", "There are no transactions for the given filters.", HttpStatus.BAD_REQUEST),
    EXCEEDED_MAX_TRANSACTION_SEARCH_PERIOD("3020", "Maximum search span cannot exceed one year.", HttpStatus.BAD_REQUEST),
    IDEMPOTENCY_VIOLATION("3021", "Keys for idempotent requests can only be used with the same parameters they were first used with. Try using the other key if you meant to execute a different request.", HttpStatus.BAD_REQUEST),
    DYNAMIC_CONFIG_NOT_FOUND("3022", "Dynamic configuration (token, returnUrl, channel) must be supplied when using Adyen as a PSP.", HttpStatus.BAD_REQUEST),
    CONFIG_NOT_FOUND("3023", "Required configurations for specified payment method are not complete", HttpStatus.BAD_REQUEST),
    PSP_ALIAS_NOT_FOUND("3024", "PSP alias is required for this PSP", HttpStatus.BAD_REQUEST),

    PSP_MODULE_ERROR("4000", "Unexpected PSP operation error.", HttpStatus.INTERNAL_SERVER_ERROR),

    SDK_GENERAL_ERROR("5000", "Unexpected SDK error.", HttpStatus.INTERNAL_SERVER_ERROR)
}
