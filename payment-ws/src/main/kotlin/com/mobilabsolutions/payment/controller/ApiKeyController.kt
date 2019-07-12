/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.request.ApiKeyEditRequestModel
import com.mobilabsolutions.payment.model.request.ApiKeyRequestModel
import com.mobilabsolutions.payment.service.ApiKeyService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(ApiKeyController.BASE_URL)
class ApiKeyController(private val apiKeyService: ApiKeyService) {

    @ApiOperation(value = "Get all api keys for specific merchant")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully fetched the api key"),
        ApiResponse(code = 400, message = "Merchant does not exist"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getApiKey(
        @PathVariable("Merchant-Id") merchantId: String
    ) = apiKeyService.getMerchantApiKeyInfo(merchantId)

    @ApiOperation(value = "Create api key for specific merchant")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully created an api key"),
        ApiResponse(code = 400, message = "Merchant does not exist"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(method = [RequestMethod.POST],
            consumes = [MediaType.APPLICATION_JSON_VALUE],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun createApiKey(
        @PathVariable("Merchant-Id") merchantId: String,
        @Valid @ApiParam(name = "Api-Key-Info", value = "Api Key Model") @RequestBody apiKeyInfo: ApiKeyRequestModel
    ) = apiKeyService.createMerchantApiKey(merchantId, apiKeyInfo)

    @ApiOperation(value = "Get api for specific merchant using id")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully fetched the api key"),
        ApiResponse(code = 400, message = "Merchant does not exist"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(API_KEY_ID_URL, method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getApiKeyById(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("API-Key-Id") apiKeyId: Long
    ) = apiKeyService.getMerchantApiKeyInfoById(apiKeyId)

    @ApiOperation(value = "Edit merchant api key name")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully edited the api key"),
        ApiResponse(code = 400, message = "Merchant does not exist"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(API_KEY_ID_URL, method = [RequestMethod.PATCH],
            consumes = [MediaType.APPLICATION_JSON_VALUE],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun editApiKeyById(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("API-Key-Id") apiKeyId: Long,
        @Valid @ApiParam(name = "Api-Key-Info", value = "Api Key Edit Model") @RequestBody apiKeyInfo: ApiKeyEditRequestModel
    ) = apiKeyService.editMerchantApiKeyInfoById(apiKeyId, apiKeyInfo)

    @ApiOperation(value = "Delete merchant api key")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully deleted the api key"),
        ApiResponse(code = 400, message = "Merchant does not exist"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(API_KEY_ID_URL, method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun deleteApiKeyById(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("API-Key-Id") apiKeyId: Long
    ) = apiKeyService.deleteMerchantApiKeyById(apiKeyId)

    companion object {
        const val BASE_URL = "merchant/{Merchant-Id}/api-key"
        const val API_KEY_ID_URL = "/{API-Key-Id}"
    }
}
