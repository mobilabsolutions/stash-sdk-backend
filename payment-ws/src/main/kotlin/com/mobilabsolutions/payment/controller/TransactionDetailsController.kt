package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.service.TransactionDetailsService
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(TransactionDetailsController.BASE_URL)
@Validated
class TransactionDetailsController(private val transactionDetailsService: TransactionDetailsService) {

    @ApiOperation(value = "Get transaction by transaction ID")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully queried transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        TransactionDetailsController.TRANSACTION_ID_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun getTransaction(
        @PathVariable(value = "Transaction-Id") transactionId: String
    ) = transactionDetailsService.getTransaction(transactionId)

    companion object {
        const val BASE_URL = "transactions"
        const val TRANSACTION_ID_URL = "/{Transaction-Id}"
    }
}
