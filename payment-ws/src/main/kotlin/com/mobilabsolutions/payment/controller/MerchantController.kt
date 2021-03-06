/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.request.MerchantRequestModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PspConfigRequestModel
import com.mobilabsolutions.payment.model.request.PspUpsertConfigRequestModel
import com.mobilabsolutions.payment.model.request.ReversalRequestModel
import com.mobilabsolutions.payment.model.request.WebhookCredentialsRequestModel
import com.mobilabsolutions.payment.service.MerchantService
import com.mobilabsolutions.payment.service.TransactionDetailsService
import com.mobilabsolutions.payment.service.TransactionService
import com.mobilabsolutions.payment.validation.DateValidator
import com.mobilabsolutions.payment.validation.PaymentMethodEnumValidator
import com.mobilabsolutions.payment.validation.TransactionActionEnumValidator
import com.mobilabsolutions.payment.validation.TransactionStatusEnumValidator
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid
import javax.validation.constraints.Size

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@RestController
@RequestMapping(MerchantController.BASE_MERCHANT_URL)
@Validated
class MerchantController(
    private val merchantService: MerchantService,
    private val transactionDetailsService: TransactionDetailsService,
    private val transactionService: TransactionService
) {
    companion object {
        const val BASE_MERCHANT_URL = "merchant"
        const val MERCHANT_CONFIG_URL = "/{Merchant-Id}/psp"
        const val MERCHANT_PSP_CONFIG_URL = "/{Merchant-Id}/psp/{Psp-Id}"
        const val TRANSACTION_DETAILS_URL = "/{Merchant-Id}/transactions/{Transaction-Id}"
        const val TRANSACTION_URL = "/{Merchant-Id}/transactions"
        const val TRANSACTION_CSV_URL = "/{Merchant-Id}/transactions/csv"
        const val CAPTURE_URL = "/{Merchant-Id}/preauthorization/{Transaction-Id}/capture"
        const val REVERSE_URL = "/{Merchant-Id}/preauthorization/{Transaction-Id}/reverse"
        const val REFUND_URL = "/{Merchant-Id}/authorization/{Transaction-Id}/refund"
        const val LOGO_URL = "/{Merchant-Id}/logo"
        const val NOTIFICATIONS_URL = "/{Merchant-Id}/notifications"

        const val CSV_CONTENT_TYPE = "text/csv"
        const val CSV_HEADER_KEY = "Content-Disposition"
        const val CSV_HEADER_VALUE = "attachment; filename=Transactions.csv"
    }

    @ApiOperation(value = "Create merchant")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully created a merchant"),
        ApiResponse(code = 400, message = "Failed to create merchant"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin')")
    fun createMerchant(
        @Valid @ApiParam(name = "Merchant-Info", value = "Merchant Model") @RequestBody merchantInfo: MerchantRequestModel
    ) = merchantService.createMerchant(merchantInfo)

    @ApiOperation(value = "Adds PSP configuration for specific merchant")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully added PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(
        MERCHANT_CONFIG_URL, method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun createPspConfigToMerchant(
        @PathVariable("Merchant-Id") merchantId: String,
        @Valid @ApiParam(name = "PSP-Config-Info", value = "PSP Config Model") @RequestBody pspConfigRequestModel: PspConfigRequestModel
    ) = merchantService.addPspConfigForMerchant(merchantId, pspConfigRequestModel)

    @ApiOperation(value = "Deletes PSP configuration for specific merchant")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully deleted PSP Configuration"),
        ApiResponse(code = 400, message = "Failed to delete PSP Configuration"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(MERCHANT_PSP_CONFIG_URL, method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun deletePspConfigToMerchant(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("Psp-Id") pspId: String
    ) = merchantService.deletePspConfigForMerchant(merchantId, pspId)

    @ApiOperation(value = "Gets list of PSP configurations for a specific merchant")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(
        MERCHANT_CONFIG_URL, method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getMerchantConfiguration(
        @PathVariable("Merchant-Id") merchantId: String
    ) = merchantService.getMerchantConfiguration(merchantId)

    @ApiOperation(value = "Gets PSP configuration a specific merchant")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        MERCHANT_PSP_CONFIG_URL, method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getMerchantConfiguration(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("Psp-Id") pspId: String
    ) = merchantService.getMerchantPspConfiguration(merchantId, pspId)

    @ApiOperation(value = "Updates PSP configuration for a specific erchant")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully updated PSP Configuration"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        MERCHANT_PSP_CONFIG_URL, method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun updateMerchantPspConfiguration(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("Psp-Id") pspId: String,
        @Valid @ApiParam(name = "PSP-Config-Info", value = "PSP Config Edit Model") @RequestBody pspUpsertConfigRequestModel: PspUpsertConfigRequestModel
    ) = merchantService.updatePspConfig(merchantId, pspId, pspUpsertConfigRequestModel)

    @ApiOperation(value = "Gets transaction details for a specific transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully queried transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        TRANSACTION_DETAILS_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getTransaction(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable(value = "Transaction-Id") transactionId: String
    ) = transactionDetailsService.getTransactionDetails(merchantId, transactionId)

    @ApiOperation(value = "Gets filtered transactions")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully queried transactions"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        TRANSACTION_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getTransactionsByFilters(
        @PathVariable("Merchant-Id") merchantId: String,
        @DateValidator @RequestParam(required = false) createdAtStart: String?,
        @DateValidator @RequestParam(required = false) createdAtEnd: String?,
        @ApiParam(value = "Payment method", example = "Values: CC, SEPA, PAY_PAL, GOOGLE_PAY, APPLE_PAY, KLARNA") @PaymentMethodEnumValidator(PaymentMethod = PaymentMethod::class) @RequestParam(required = false) paymentMethod: String?,
        @ApiParam(value = "Transaction action", example = "Values: PREAUTH, AUTH, REVERSAL, REFUND, CAPTURE") @TransactionActionEnumValidator(TransactionAction = TransactionAction::class) @RequestParam(required = false) action: String?,
        @ApiParam(value = "Transaction status", example = "Values: SUCCESS, FAIL") @TransactionStatusEnumValidator(TransactionStatus = TransactionStatus::class) @RequestParam(required = false) status: String?,
        @RequestParam(required = false) text: String?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Int?
    ) = transactionDetailsService.getTransactionsByFilters(merchantId, createdAtStart, createdAtEnd, paymentMethod,
        action, status, text, limit ?: 10, offset ?: 0)

    @ApiOperation(value = "Export transactions to CSV file")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully exported transactions"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        TRANSACTION_CSV_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun downloadTransactionsCsv(
        response: HttpServletResponse,
        @PathVariable("Merchant-Id") merchantId: String,
        @DateValidator @RequestParam(required = false) createdAtStart: String?,
        @DateValidator @RequestParam(required = false) createdAtEnd: String?,
        @RequestParam(required = false) paymentMethod: String?,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) text: String?,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) offset: Int?
    ) {
        response.contentType = CSV_CONTENT_TYPE
        response.setHeader(CSV_HEADER_KEY, CSV_HEADER_VALUE)
        transactionDetailsService.writeTransactionsToCsv(response, merchantId, createdAtStart, createdAtEnd,
            paymentMethod, action, status, text, limit ?: 10, offset ?: 0)
    }

    @ApiOperation(value = "Capture transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully captured transaction"),
        ApiResponse(code = 400, message = "Failed to capture transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(CAPTURE_URL, method = [RequestMethod.PUT])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun captureTransaction(
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @PathVariable(value = "Merchant-Id") merchantId: String,
        @PathVariable(value = "Transaction-Id") transactionId: String
    ) = transactionService.dashboardCapture(merchantId, pspTestMode, transactionId)

    @ApiOperation(value = "Reverse transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully reversed transaction"),
        ApiResponse(code = 400, message = "Failed to reverse transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(REVERSE_URL, method = [RequestMethod.PUT])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun reverseTransaction(
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @PathVariable(value = "Merchant-Id") merchantId: String,
        @PathVariable(value = "Transaction-Id") transactionId: String,
        @Valid @ApiParam(name = "Reverse-Info", value = "Reverse Info Model") @RequestBody reverseInfo: ReversalRequestModel
    ) = transactionService.dashboardReverse(merchantId, pspTestMode, transactionId, reverseInfo)

    @ApiOperation(value = "Refund transaction")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully refunded transaction"),
        ApiResponse(code = 400, message = "Failed to refund transaction"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 404, message = "Not found")
    )
    @RequestMapping(REFUND_URL, method = [RequestMethod.PUT])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun refundTransaction(
        @Size(min = 10, max = 40) @RequestHeader(value = "Idempotent-Key") idempotentKey: String,
        @RequestHeader(value = "PSP-Test-Mode", required = false) pspTestMode: Boolean?,
        @PathVariable(value = "Merchant-Id") merchantId: String,
        @PathVariable(value = "Transaction-Id") transactionId: String,
        @Valid @ApiParam(name = "Refund-Info", value = "Refund Info Model") @RequestBody refundInfo: PaymentDataRequestModel
    ) = transactionService.dashboardRefund(merchantId, idempotentKey, pspTestMode, transactionId, refundInfo)

    @ApiOperation(value = "Save merchant logo")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully saved merchant logo"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        LOGO_URL,
        method = [RequestMethod.POST]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun uploadAndSaveLogo(
        @PathVariable("Merchant-Id") merchantId: String,
        @RequestParam file: MultipartFile
    ) = merchantService.uploadAndSaveLogo(merchantId, file)

    @ApiOperation(value = "Get merchant logo")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully queried merchant logo"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        LOGO_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getLogo(
        @PathVariable("Merchant-Id") merchantId: String
    ) = merchantService.getLogo(merchantId)

    @ApiOperation(value = "Create merchant webhook credentials")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully created merchant webhook credentials"),
        ApiResponse(code = 400, message = "Failed to create merchant webhook credentials"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access")
    )
    @RequestMapping(MerchantController.NOTIFICATIONS_URL, method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun createWebhookCredentials(
        @PathVariable(value = "Merchant-Id") merchantId: String,
        @ApiParam(name = "Webhook-Credentials", value = "Webhook credentials model") @RequestBody webhookCredentialsRequest: WebhookCredentialsRequestModel
    ) = merchantService.createWebhookCredentials(merchantId, webhookCredentialsRequest)
}
