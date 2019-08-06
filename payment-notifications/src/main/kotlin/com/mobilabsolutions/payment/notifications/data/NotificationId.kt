/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.data

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Embeddable
class NotificationId(
    @Column(name = "psp_transaction_id")
    var pspTransactionId: String?,
    @Column(name = "psp_event")
    var pspEvent: String?
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationId

        if (pspTransactionId != other.pspTransactionId) return false
        if (pspEvent != other.pspEvent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pspTransactionId?.hashCode() ?: 0
        result = 31 * result + (pspEvent?.hashCode() ?: 0)
        return result
    }
}
