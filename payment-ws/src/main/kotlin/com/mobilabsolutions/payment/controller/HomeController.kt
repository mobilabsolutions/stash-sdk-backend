package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.service.HomeService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@RestController
@RequestMapping(HomeController.BASE_HOME_URL)
@Validated
class HomeController(
    private val homeService: HomeService
) {

    companion object {
        const val BASE_HOME_URL = "home"
        const val KEY_PERFORMANCE_URL = "/{Merchant-Id}/key-performance"
        const val NOTIFICATIONS_URL = "/{Merchant-Id}/notifications"
    }

    @ApiOperation(value = "Get key performance")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully returned key performance"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(KEY_PERFORMANCE_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getKeyPerformance(
        @PathVariable("Merchant-Id") merchantId: String
    ) = homeService.getKeyPerformance(merchantId)

    @ApiOperation(value = "Get notifications")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully returned key performance"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(NOTIFICATIONS_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getNotifications(
        @PathVariable("Merchant-Id") merchantId: String
    ) = homeService.getNotifications(merchantId)
}
