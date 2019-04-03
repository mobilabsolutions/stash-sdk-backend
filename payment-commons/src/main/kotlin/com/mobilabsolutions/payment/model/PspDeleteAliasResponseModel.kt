package com.mobilabsolutions.payment.model

import com.mobilabsolutions.server.commons.exception.PaymentError
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "Psp Delete Alias Response")
class PspDeleteAliasResponseModel(
    @ApiModelProperty(value = "Payment error")
    val error: PaymentError?,

    @ApiModelProperty(value = "Error message")
    val errorMessage: String?
) {
    fun hasError() = error != null
}
