package com.mobilabsolutions.payment.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.message.PspConfigMessage
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.Valid
import javax.validation.constraints.Email

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AliasRequestModel(val pspAlias: String?, @field:Valid val extra: AliasExtraModel?)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AliasResponseModel(val aliasId: String?, val extra: AliasExtraModel?, val psp: PspConfigMessage?)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AliasExtraModel(@field:Email val email: String?, val ccMask: String?, val ccExpiry: String?, val ccType: String?, val ibanMask: String?, @field:Enumerated(EnumType.STRING) val paymentMethod: PaymentMethod?)