/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PspConfigModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP payment request model")
data class PspPaymentRequestModel(
    @ApiModelProperty("Alias ID", example = "JipfjLKL6BkTIREaRGyX")
    val aliasId: String?,

    @ApiModelProperty(value = "Alias extra model")
    @field:Valid
    val extra: AliasExtraModel?,

    @ApiModelProperty("Payment data request model")
    @field:Valid
    val paymentData: PaymentDataRequestModel?,

    @ApiModelProperty(value = "PSP alias", example = "jsklcmn")
    val pspAlias: String?,

    @ApiModelProperty(value = "PSP config model")
    val pspConfig: PspConfigModel?,

    @ApiModelProperty("Purchase ID", example = "132")
    val purchaseId: String?
)
