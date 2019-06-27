package com.mobilabsolutions.payment.notifications.model

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AdyenNotificationItemModel(
    val additionalData: AdyenNotificationItemAdditionalDataModel?,
    val amount: AdyenAmountModel?,
    val pspReference: String?,
    val eventCode: String?,
    val eventDate: String?,
    val merchantAccountCode: String?,
    val operations: MutableList<String>?,
    val merchantReference: String?,
    val originalReference: String?,
    val paymentMethod: String?,
    val reason: String?,
    val success: String?
)