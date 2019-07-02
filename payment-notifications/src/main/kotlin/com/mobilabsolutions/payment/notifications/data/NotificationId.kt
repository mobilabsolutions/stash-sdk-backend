package com.mobilabsolutions.payment.notifications.data

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Embeddable
class NotificationId (
    @Column(name = "psp_transaction_id")
    var pspTransactionId: String?,
    @Column(name = "psp_event")
    var pspEvent: String?
): Serializable
