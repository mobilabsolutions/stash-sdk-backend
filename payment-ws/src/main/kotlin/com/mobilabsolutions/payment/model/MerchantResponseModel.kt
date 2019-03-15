package com.mobilabsolutions.payment.model

import javax.validation.constraints.Email

data class MerchantResponseModel(
    val name: String?,
    @field:Email val
    email: String?,
    val defaultCurrency: String?
)
