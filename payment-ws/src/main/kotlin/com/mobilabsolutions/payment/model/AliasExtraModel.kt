package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.Email

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AliasExtraModel(
    @field:Email val email: String?,
    val ccMask: String?,
    val ccExpiry: String?,
    val ccType: String?,
    val ibanMask: String?,
    @field:Enumerated(EnumType.STRING)
    val paymentMethod: PaymentMethod?
)