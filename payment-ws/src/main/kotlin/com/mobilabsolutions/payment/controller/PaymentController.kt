package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.PaymentDataModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.ReversalRequestModel
import com.mobilabsolutions.payment.service.TransactionService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
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
    @ResponseStatus(HttpStatus.CREATED)
    fun preauthorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @Size(min = 10, max = 20) @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @Valid @RequestBody preauthorizeInfo: PaymentRequestModel
    ) = transactionService.preauthorize(secretKey, idempotentKey, pspTestMode, preauthorizeInfo)

    @ApiOperation(value = "Capture transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully captured transaction"),
        ApiResponse(code = 400, message = "Failed to capture transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(PaymentController.CAPTURE_URL, method = [RequestMethod.PUT])
    @ResponseStatus(HttpStatus.OK)
    fun captureTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @PathVariable(value = "Transaction-Id") transactionId: String
    ) = transactionService.capture(secretKey, pspTestMode, transactionId)

    @ApiOperation(value = "Authorize transaction")
    @ApiResponses(
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
    @ResponseStatus(HttpStatus.CREATED)
    fun authorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @Size(min = 10, max = 20) @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @Valid @RequestBody authorizeInfo: PaymentRequestModel
    ) = transactionService.authorize(secretKey, idempotentKey, pspTestMode, authorizeInfo)

    @ApiOperation(value = "Reverse transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully reversed transaction"),
        ApiResponse(code = 400, message = "Failed to reverse transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(
        PaymentController.REVERSE_URL,
        method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    fun reverseTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @PathVariable(value = "Transaction-Id") transactionId: String,
        @RequestBody reverseInfo: ReversalRequestModel
    ) = transactionService.reverse(secretKey, pspTestMode, transactionId, reverseInfo)

    @ApiOperation(value = "Refund transaction")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully refunded transaction"),
        ApiResponse(code = 400, message = "Failed to refund transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(
        PaymentController.REFUND_URL,
        method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun refundTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @Size(min = 10, max = 20) @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @PathVariable(value = "Transaction-Id") transactionId: String,
        @RequestBody refundInfo: PaymentDataModel
    ) = transactionService.refund(secretKey, idempotentKey, pspTestMode, transactionId, refundInfo)

    companion object {
        const val PREAUTH_URL = "preauthorization"
        const val CAPTURE_URL = "preauthorization/{Transaction-Id}/capture"
        const val AUTH_URL = "authorization"
        const val REVERSE_URL = "preauthorization/{Transaction-Id}/reverse"
        const val REFUND_URL = "authorization/{Transaction-Id}/refund"
    }
}
