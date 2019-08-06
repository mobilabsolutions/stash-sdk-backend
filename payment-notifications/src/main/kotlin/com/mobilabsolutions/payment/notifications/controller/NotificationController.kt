/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.controller

import com.mobilabsolutions.payment.adyen.model.request.AdyenNotificationRequestModel
import com.mobilabsolutions.payment.notifications.service.NotificationService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@RestController
@RequestMapping
@Validated
class NotificationController(
    private val notificationService: NotificationService
) {
    companion object {
        const val ADYEN__URL = "adyen"
    }

    @ApiOperation(value = "Create Adyen notification")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully created Adyen notification"),
        ApiResponse(code = 400, message = "Failed to create Adyen notification"),
        ApiResponse(code = 401, message = "Unauthorized access")
    )
    @RequestMapping(
        ADYEN__URL, method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    fun createAdyenNotification(
        @Valid @ApiParam(name = "Adyen-Notification-Info", value = "Adyen Notification Model") @RequestBody adyenNotificationRequestModel: AdyenNotificationRequestModel?
    ) = notificationService.saveAdyenNotifications(adyenNotificationRequestModel)
}
