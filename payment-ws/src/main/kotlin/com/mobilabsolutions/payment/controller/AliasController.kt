package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.service.AliasService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
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
@RequestMapping(AliasController.BASE_URL)
class AliasController(private val aliasService: AliasService) {

    @ApiOperation(value = "Create an Alias for payment operations")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully created an Alias")
    )
    @RequestMapping(method = [RequestMethod.POST],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createAlias(
        @RequestHeader(value = "Publishable-Key") publishableKey: String,
        @RequestHeader(value = "PSP-Type") pspType: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?
    ) = aliasService.createAlias(publishableKey, pspType, pspTestMode)

    @ApiOperation(value = "Update the given Alias for payment operations")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully updated an Alias"),
        ApiResponse(code = 400, message = "Request model validation is failed")
    )
    @RequestMapping(EXCHANGE_ALIAS_URL, method = [RequestMethod.PUT],
            consumes = [MediaType.APPLICATION_JSON_VALUE],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun exchangeAlias(
        @RequestHeader(value = "Publishable-Key") publishableKey: String,
        @PathVariable("Alias-Id") aliasId: String,
        @Valid @RequestBody alias: AliasRequestModel
    ) = aliasService.exchangeAlias(publishableKey, aliasId, alias)

    @ApiOperation(value = "Delete an Alias")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully deleted an Alias"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(DELETE_ALIAS_URL, method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAlias(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @PathVariable("Alias-Id") aliasId: String
    ) = aliasService.deleteAlias(secretKey, aliasId)

    companion object {
        const val BASE_URL = "alias"
        const val EXCHANGE_ALIAS_URL = "/{Alias-Id}"
        const val DELETE_ALIAS_URL = "/{Alias-Id}"
    }
}