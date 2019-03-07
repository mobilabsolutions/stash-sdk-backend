package com.mobilabsolutions.payment.model

import com.mobilabsolutions.payment.message.PspConfigMessage

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AliasResponseModel(
    val aliasId: String?,
    val extra: AliasExtraModel?,
    val psp: PspConfigMessage?
)