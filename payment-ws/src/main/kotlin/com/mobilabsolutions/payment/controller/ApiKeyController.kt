package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.service.ApiKeyService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(ApiKeyController.BASE_URL)
class ApiKeyController(private val apiKeyService: ApiKeyService) {

    @ApiOperation(value = "Get api key for specific merchant")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully fetched api key")
    )
    @RequestMapping(method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    fun getApiKey(
        @PathVariable("Merchant-Id") merchantId: String
    ) = apiKeyService.getMerchantApiKeyInfo(merchantId)

    @ApiOperation(value = "Create api key for specific merchant")
    @ApiResponses(
        ApiResponse(code = 201, message = "Succesfully created an api key")
    )
    @RequestMapping(method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.CREATED)
    fun createApiKey(
        @PathVariable("Merchant-Id") merchantId: String,
        @RequestHeader(value = "Type") apiKeyType: KeyType?,
        @RequestHeader(value = "Name") apiKeyName: String
    ) = apiKeyService.createMerchantApiKey(merchantId, apiKeyType, apiKeyName)

    @ApiOperation(value = "Get api for specific merchant using id")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully fetched api key")
    )
    @RequestMapping(API_KEY_ID_URL, method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    fun getApiKeyById(
        @PathVariable("API-Key-Id") apiKeyId: Long
    ) = apiKeyService.getMerchantApiKeyInfoById(apiKeyId)

    @ApiOperation(value = "Edit merchant api key name")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully edited api key")
    )
    @RequestMapping(API_KEY_ID_URL, method = [RequestMethod.PATCH])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun editApiKeyById(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("API-Key-Id") apiKeyId: Long,
        @RequestHeader(value = "Name") apiKeyName: String
    ) = apiKeyService.editMerchantApiKeyInfoById(apiKeyId, apiKeyName)

    @ApiOperation(value = "Delete merchant api key")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully deleted api key")
    )
    @RequestMapping(API_KEY_ID_URL, method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteApiKeyById(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("API-Key-Id") apiKeyId: Long
    ) = apiKeyService.deleteMerchantApiKeyById(apiKeyId)

    companion object {
        const val BASE_URL = "merchant/{Merchant-Id}/api-key"
        const val API_KEY_ID_URL = "/{API-Key-Id}"
    }
}