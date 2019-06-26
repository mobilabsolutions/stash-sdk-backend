package com.mobilabsolutions.payment.notifications.controller

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.notifications.model.request.AdyenNotificationRequestModel
import com.mobilabsolutions.payment.notifications.service.NotificationService
import com.mobilabsolutions.payment.validation.PaymentServiceProviderEnumValidator
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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
        const val PROCESS_URL = "process" // for testing
        const val ADYEN__URL = "adyen"
    }

    @ApiOperation(value = "Create merchant")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully created merchant"),
        ApiResponse(code = 400, message = "Failed to create merchant"),
        ApiResponse(code = 401, message = "Unauthorized access")
    )
    @RequestMapping(
        ADYEN__URL, method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    fun createAdyenNotification(
        @Valid @RequestBody adyenNotificationRequestModel: AdyenNotificationRequestModel
    ) = notificationService.saveAdyenNotifications(adyenNotificationRequestModel)

    @RequestMapping(
        PROCESS_URL, method = [RequestMethod.GET]
    )
    @ResponseStatus(HttpStatus.OK)
    fun processNotifications(
        @PaymentServiceProviderEnumValidator(PaymentServiceProvider = PaymentServiceProvider::class) @RequestHeader(value = "PSP-Type") pspType: String
    ) = notificationService.pickNotification(pspType)
}
