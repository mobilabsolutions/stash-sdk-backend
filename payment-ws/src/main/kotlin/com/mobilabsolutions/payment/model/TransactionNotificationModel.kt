/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel("Transaction notification model")
data class TransactionNotificationModel(
    @ApiModelProperty(value = "Transaction id")
    val id: Long?,

    @ApiModelProperty(value = "Merchant id")
    val merchantId: String?
)
