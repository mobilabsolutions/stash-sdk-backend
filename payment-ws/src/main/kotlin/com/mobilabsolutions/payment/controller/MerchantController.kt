package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.request.MerchantRequestModel
import com.mobilabsolutions.payment.model.request.PspConfigRequestModel
import com.mobilabsolutions.payment.model.request.PspUpsertConfigRequestModel
import com.mobilabsolutions.payment.service.MerchantService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@RestController
@RequestMapping(MerchantController.BASE_MERCHANT_URL)
class MerchantController(private val merchantService: MerchantService) {
    companion object {
        const val BASE_MERCHANT_URL = "merchant"
        const val MERCHANT_CONFIG_URL = "/{Merchant-Id}/psp"
        const val MERCHANT_PSP_CONFIG_URL = "/{Merchant-Id}/psp/{Psp-Id}"
    }

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
        @Valid @RequestBody merchantInfo: MerchantRequestModel
    ) = merchantService.createMerchant(merchantInfo)

    @ApiOperation(value = "Add PSP Configuration for the Merchant")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully added PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(MERCHANT_CONFIG_URL, method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun createPspConfigToMerchant(
        @PathVariable("Merchant-Id") merchantId: String,
        @Valid @RequestBody pspConfigRequestModel: PspConfigRequestModel
    ) = merchantService.addPspConfigForMerchant(merchantId, pspConfigRequestModel)

    @ApiOperation(value = "Get List of PSP Configuration for the Merchant")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(MERCHANT_CONFIG_URL, method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getMerchantConfiguration(
        @PathVariable("Merchant-Id") merchantId: String
    ) = merchantService.getMerchantConfiguration(merchantId)

    @ApiOperation(value = "Get PSP Configuration for the Merchant")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(MERCHANT_PSP_CONFIG_URL, method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getMerchantConfiguration(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("Psp-Id") pspId: String
    ) = merchantService.getMerchantPspConfiguration(merchantId, pspId)

    @ApiOperation(value = "Update PSP Configuration for the Merchant")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully updated PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(MERCHANT_PSP_CONFIG_URL, method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun updateMerchantPspConfiguration(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("Psp-Id") pspId: String,
        @Valid @RequestBody pspUpsertConfigRequestModel: PspUpsertConfigRequestModel
    ) = merchantService.updatePspConfig(merchantId, pspId, pspUpsertConfigRequestModel)
}
