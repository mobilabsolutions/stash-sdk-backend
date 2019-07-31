package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.service.HomeService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(HomeController.BASE_HOME_URL)
@Validated
class HomeController(
    private val homeService: HomeService
) {
    companion object {
        const val BASE_HOME_URL = "home"
        const val REFUND_URL = "/{Merchant-Id}/refunds"
    }

    @ApiOperation(value = "Refunds overview")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved refunds"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        HomeController.REFUND_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    fun getRefundsOverview(
        @PathVariable("Merchant-Id") merchantId: String
    ) = homeService.getRefundsOverview(merchantId)
}
