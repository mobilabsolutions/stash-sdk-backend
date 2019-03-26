package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.AuthorizeRequestModel
import com.mobilabsolutions.payment.model.AuthorizeResponseModel
import com.mobilabsolutions.payment.service.TransactionService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@RestController
@RequestMapping
class PaymentController(private val transactionService: TransactionService) {

    @ApiOperation(value = "Preauthorize transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Preauthorization check successful"),
        ApiResponse(code = 201, message = "Successfully preauthorized transaction"),
        ApiResponse(code = 400, message = "Failed to preauthorize transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(PaymentController.PREAUTH_URL, method = [RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun preauthorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @Valid @RequestBody authorizeInfo: AuthorizeRequestModel
    ): ResponseEntity<AuthorizeResponseModel> =
        transactionService.preauthorize(secretKey, idempotentKey, authorizeInfo)

    @ApiOperation(value = "Authorize transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Authorization check successful"),
        ApiResponse(code = 201, message = "Successfully authorized transaction"),
        ApiResponse(code = 400, message = "Failed to authorize transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(PaymentController.AUTH_URL, method = [RequestMethod.PUT], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun authorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @Valid @RequestBody authorizeInfo: AuthorizeRequestModel
    ): ResponseEntity<AuthorizeResponseModel> =
        transactionService.authorize(secretKey, idempotentKey, authorizeInfo)

    companion object {
        const val PREAUTH_URL = "preauthorization"
        const val AUTH_URL = "authorization"
    }
}