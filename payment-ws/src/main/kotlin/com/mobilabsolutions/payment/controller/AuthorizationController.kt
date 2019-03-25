package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.AuthorizeRequestModel
import com.mobilabsolutions.payment.model.AuthorizeResponseModel
import com.mobilabsolutions.payment.service.AuthorizationService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(AuthorizationController.BASE_URL)
class AuthorizationController(private val authorizationService: AuthorizationService) {

    @ApiOperation(value = "Authorize transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Authorization check successful"),
        ApiResponse(code = 201, message = "Successfully authorized transaction"),
        ApiResponse(code = 400, message = "Failed to authorize transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(method = [RequestMethod.PUT])
    fun authorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @Valid @RequestBody authorizeInfo: AuthorizeRequestModel
    ): ResponseEntity<AuthorizeResponseModel> = authorizationService.authorize(secretKey, idempotentKey, authorizeInfo)

    companion object {
        const val BASE_URL = "authorization"
    }
}