package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.controller.ReportController.Companion.BASE_REPORT_URL
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.ReportType
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.service.ReportService
import com.mobilabsolutions.payment.validation.DateValidator
import com.mobilabsolutions.payment.validation.PaymentMethodEnumValidator
import com.mobilabsolutions.payment.validation.ReportTypeValidator
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletResponse
import java.util.Date

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@RestController
@RequestMapping(BASE_REPORT_URL)
@Validated
class ReportController(
    private val reportService: ReportService
) {
    companion object {
        const val BASE_REPORT_URL = "report"
        const val EXPORT_DEFAULT_REPORT_URL = "/{Merchant-Id}/default"
        const val EXPORT_CUSTOM_REPORT_URL = "/{Merchant-Id}/custom"
        const val DELETE_FILTER_URL = "/{Merchant-Id}"

        const val CSV_CONTENT_TYPE = "text/csv"
        const val CSV_HEADER_KEY = "Content-Disposition"
    }

    @ApiOperation(value = "Export default report to CSV file")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully exported report"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(EXPORT_DEFAULT_REPORT_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun downloadDefaultReports(
        response: HttpServletResponse,
        @PathVariable("Merchant-Id") merchantId: String,
        @ApiParam(value = "Report type", example = "Values: OVERVIEW, REFUND, CHARGEBACK") @ReportTypeValidator(ReportType = ReportType::class) @RequestParam(required = false) reportType: String?,
        @RequestParam fileName: String
    ) {
        val fileNameString = fileName + "_" + SimpleDateFormat("yyyyMMdd").format(Date())
        response.contentType = CSV_CONTENT_TYPE
        response.setHeader(CSV_HEADER_KEY, "attachment; filename=$fileNameString.csv")
        reportService.downloadDefaultReports(response, reportType, merchantId)
    }

    @ApiOperation(value = "Export custom report to CSV file")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully exported report"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(EXPORT_CUSTOM_REPORT_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun downloadCustomReports(
        response: HttpServletResponse,
        @PathVariable("Merchant-Id") merchantId: String,
        @RequestParam filterName: String,
        @DateValidator @RequestParam(required = false) createdAtStart: String?,
        @DateValidator @RequestParam(required = false) createdAtEnd: String?,
        @ApiParam(value = "Payment method", example = "Values: CC, SEPA, PAY_PAL, GOOGLE_PAY, APPLE_PAY, KLARNA") @PaymentMethodEnumValidator(PaymentMethod = PaymentMethod::class) @RequestParam(required = false) paymentMethod: String?,
        @ApiParam(value = "Transaction status", example = "Values: SUCCESS, FAIL") @TransactionStatusEnumValidator(TransactionStatus = TransactionStatus::class) @RequestParam(required = false) status: String?,
        @RequestParam(required = false) text: String?,
        @RequestParam(required = false) currency: String?,
        @RequestParam(required = false) amount: String?,
        @RequestParam(required = false) customerId: String?,
        @RequestParam(required = false) transactionId: String?,
        @RequestParam(required = false) merchantTransactionId: String?
    ) {
        val fileNameString = filterName + "_" + SimpleDateFormat("yyyyMMdd").format(Date())
        response.contentType = CSV_CONTENT_TYPE
        response.setHeader(CSV_HEADER_KEY, "attachment; filename=$fileNameString.csv")
        reportService.downloadCustomReports(response, merchantId, filterName, createdAtStart, createdAtEnd, paymentMethod, status, text, currency, amount, customerId, transactionId, merchantTransactionId)
    }

    @ApiOperation(value = "Delete a report filter")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully deleted report filter"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(DELETE_FILTER_URL,
        method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getAllReportFilters(
        @PathVariable("Merchant-Id") merchantId: String,
        @RequestParam filterName: String
    ) = reportService.deleteReportFilter(merchantId, filterName)
}
