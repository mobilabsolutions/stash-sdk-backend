package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PaymentResponseModel
import com.mobilabsolutions.payment.service.TransactionService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.Size

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@RestController
@RequestMapping
@Validated
class PaymentController(private val transactionService: TransactionService) {

    @ApiOperation(value = "Preauthorize transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Preauthorization check successful"),
        ApiResponse(code = 201, message = "Successfully preauthorized transaction"),
        ApiResponse(code = 400, message = "Failed to preauthorize transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(
        PaymentController.PREAUTH_URL,
        method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun preauthorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @Size(min = 5, max = 10) @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @Valid @RequestBody preauthorizeInfo: PaymentRequestModel
    ): ResponseEntity<PaymentResponseModel> = transactionService.preauthorize(secretKey, idempotentKey, pspTestMode, preauthorizeInfo)

    @ApiOperation(value = "Capture transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully captured transaction"),
        ApiResponse(code = 400, message = "Failed to capture transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(PaymentController.CAPTURE_URL, method = [RequestMethod.PUT])
    fun captureTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @PathVariable(value = "Transaction-Id") transactionId: String
    ): ResponseEntity<PaymentResponseModel> = transactionService.capture(secretKey, pspTestMode, transactionId)

    @ApiOperation(value = "Authorize transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Authorization check successful"),
        ApiResponse(code = 201, message = "Successfully authorized transaction"),
        ApiResponse(code = 400, message = "Failed to authorize transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(
        PaymentController.AUTH_URL,
        method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun authorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @Size(min = 5, max = 10) @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @Valid @RequestBody authorizeInfo: PaymentRequestModel
    ): ResponseEntity<PaymentResponseModel> = transactionService.authorize(secretKey, idempotentKey, pspTestMode, authorizeInfo)

    companion object {
        const val PREAUTH_URL = "preauthorization"
        const val CAPTURE_URL = "preauthorization/{Transaction-Id}/capture"
        const val AUTH_URL = "authorization"
    }
}