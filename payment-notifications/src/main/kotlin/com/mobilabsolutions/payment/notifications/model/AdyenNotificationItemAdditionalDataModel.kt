package com.mobilabsolutions.payment.notifications.model

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AdyenNotificationItemAdditionalDataModel(
    val shopperReference: String?,
    val shopperEmail: String?,
    val authCode: String?,
    val cardSummary: String?,
    val expiryDate: String?,
    val authorisedAmountValue: String?,
    val authorisedAmountCurrency: String?
)
