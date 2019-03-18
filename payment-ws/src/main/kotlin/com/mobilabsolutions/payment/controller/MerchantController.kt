package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.MerchantRequestModel
import com.mobilabsolutions.payment.service.MerchantService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(MerchantController.BASE_URL)
class MerchantController(private val merchantService: MerchantService) {

    @ApiOperation(value = "Create merchant")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully created merchant"),
        ApiResponse(code = 400, message = "Failed to create merchant"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin')")
    fun createMerchant(
        @RequestBody merchantInfo: MerchantRequestModel
    ) = merchantService.createMerchant(merchantInfo)

    companion object {
        const val BASE_URL = "merchant"
    }
}