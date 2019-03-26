package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.CaptureResponseModel
import com.mobilabsolutions.payment.model.PreauthorizeRequestModel
import com.mobilabsolutions.payment.model.PreauthorizeResponseModel
import com.mobilabsolutions.payment.service.PreauthorizationService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import javax.validation.Valid

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(PreAuthorizationController.BASE_URL)
class PreAuthorizationController(private val preauthorizationService: PreauthorizationService) {

    @ApiOperation(value = "Authorize transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Authorization check successful"),
        ApiResponse(code = 201, message = "Successfully authorized transaction"),
        ApiResponse(code = 400, message = "Failed to authorize transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(method = [RequestMethod.PUT])
    fun preauthorizeTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @Valid @RequestBody preauthorizeInfo: PreauthorizeRequestModel
    ): ResponseEntity<PreauthorizeResponseModel> =
        preauthorizationService.preauthorize(secretKey, idempotentKey, preauthorizeInfo)

    @ApiOperation(value = "Capture transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Capture check successsful"),
        ApiResponse(code = 201, message = "Successfully captured transaction"),
        ApiResponse(code = 400, message = "Failed to capture transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(CAPTURE_URL, method = [RequestMethod.PUT])
    fun captureTransaction(
        @RequestHeader(value = "Secret-Key") secretKey: String,
        @PathVariable(value = "Transaction-Id") transactionId: String
    ): ResponseEntity<CaptureResponseModel> =
        preauthorizationService.capture(secretKey, transactionId)

    companion object {
        const val BASE_URL = "preauthorization"
        const val CAPTURE_URL = "{Transaction-Id}/capture"
    }
}