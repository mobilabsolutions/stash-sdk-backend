package com.mobilabsolutions.payment.model

import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
data class AliasRequestModel(
    @field:NotNull val pspAlias: String?,
    @field:Valid val extra: AliasExtraModel?
)