/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.model.PspNotificationModel
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ApiModel(value = "PSP notification list request model")
data class PspNotificationListRequestModel(
    @ApiModelProperty(value = "List of PSP notifications")
    @field:Valid
    val notifications: MutableList<PspNotificationModel> = mutableListOf()
)
