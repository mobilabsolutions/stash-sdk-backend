package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.service.HomeService
import com.mobilabsolutions.payment.validation.DateValidator
import com.mobilabsolutions.payment.validation.PaymentMethodEnumValidator
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
@RequestMapping(HomeController.BASE_HOME_URL)
@Validated
class HomeController(
    private val homeService: HomeService
) {

    companion object {
        const val BASE_HOME_URL = "home"
        const val KEY_PERFORMANCE_URL = "/{Merchant-Id}/key-performance"
        const val NOTIFICATIONS_URL = "/{Merchant-Id}/notifications"
        const val REFUND_URL = "/{Merchant-Id}/refunds"
        const val PAYMENT_METHODS_URL = "/{Merchant-Id}/payment-methods"
        const val ACTIVITY_URL = "/{Merchant-Id}/activity"
        const val REPORTS_URL = "/{Merchant-Id}/reports"

        const val CSV_CONTENT_TYPE = "text/csv"
        const val CSV_HEADER_KEY = "Content-Disposition"
    }

    @ApiOperation(value = "Get key performance")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully returned key performance"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(KEY_PERFORMANCE_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getKeyPerformance(
        @PathVariable("Merchant-Id") merchantId: String
    ) = homeService.getKeyPerformance(merchantId)

    @ApiOperation(value = "Get notifications")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully returned notifications"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(NOTIFICATIONS_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getNotifications(
        @PathVariable("Merchant-Id") merchantId: String
    ) = homeService.getNotifications(merchantId)

    @ApiOperation(value = "Refunds overview")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved refunds"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(REFUND_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getRefundsOverview(
        @PathVariable("Merchant-Id") merchantId: String
    ) = homeService.getRefundsOverview(merchantId)

    @ApiOperation(value = "Payment methods overview")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved transactions"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(
        PAYMENT_METHODS_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getPaymentMethodsOverview(
        @PathVariable("Merchant-Id") merchantId: String
    ) = homeService.getPaymentMethodsOverview(merchantId)

    @ApiOperation(value = "Selected date activity")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully retrieved activity for the selected date"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(ACTIVITY_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun getSelectedDateActivity(
        @PathVariable("Merchant-Id") merchantId: String,
        @DateValidator @RequestParam date: String
    ) = homeService.getSelectedDateActivity(merchantId, date)

    @ApiOperation(value = "Export report to CSV file")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully exported report"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(REPORTS_URL,
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun downloadReports(
        response: HttpServletResponse,
        @PathVariable("Merchant-Id") merchantId: String,
        @RequestParam(required = false) reportType: String?,
        @RequestParam fileName: String,
        @DateValidator @RequestParam(required = false) createdAtStart: String?,
        @DateValidator @RequestParam(required = false) createdAtEnd: String?,
        @ApiParam(value = "Payment method", example = "Values: CC, SEPA, PAY_PAL, GOOGLE_PAY, APPLE_PAY, KLARNA") @PaymentMethodEnumValidator(PaymentMethod = PaymentMethod::class) @RequestParam(required = false) paymentMethod: String?,
        @ApiParam(value = "Transaction status", example = "Values: SUCCESS, FAIL") @TransactionStatusEnumValidator(TransactionStatus = TransactionStatus::class) @RequestParam(required = false) status: String?,
        @RequestParam(required = false) text: String?
    ) {
        val fileNameString = fileName + "_" + SimpleDateFormat("yyyyMMdd").format(Date())
        response.contentType = CSV_CONTENT_TYPE
        response.setHeader(CSV_HEADER_KEY, "attachment; filename=$fileNameString.csv")
        homeService.downloadReports(response, reportType, merchantId, createdAtStart, createdAtEnd, paymentMethod, status, text)
    }
}
