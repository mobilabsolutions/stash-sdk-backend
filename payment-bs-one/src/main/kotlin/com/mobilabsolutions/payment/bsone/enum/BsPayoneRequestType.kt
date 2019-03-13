package com.mobilabsolutions.payment.bsone.enum

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
enum class BsPayoneRequestType(val type: String) {
    CREDIT_CARD_CHECK("creditcardcheck"),
    PREAUTTHORIZATION("preauthorization"),
    AUTHORIZATION("authorization"),
    CAPTURE("capture"),
    REFUND("refund")
}