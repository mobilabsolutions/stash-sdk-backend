package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.controller.ReportController.Companion.BASE_HOME_URL
import com.mobilabsolutions.payment.data.enum.ReportType
import com.mobilabsolutions.payment.service.ReportService
import com.mobilabsolutions.payment.validation.ReportTypeValidator
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
@RequestMapping(BASE_HOME_URL)
@Validated
class ReportController(
    private val reportService: ReportService
) {
    companion object {
        const val BASE_HOME_URL = "report"
        const val EXPORT_REPORT_URL = "/{Merchant-Id}/default"

        const val CSV_CONTENT_TYPE = "text/csv"
        const val CSV_HEADER_KEY = "Content-Disposition"
    }

    @ApiOperation(value = "Export report to CSV file")
    @ApiResponses(
        ApiResponse(code = 200, message = "Successfully exported report"),
        ApiResponse(code = 401, message = "Unauthorized access"),
        ApiResponse(code = 403, message = "Forbidden access"),
        ApiResponse(code = 404, message = "Resource not found")
    )
    @RequestMapping(EXPORT_REPORT_URL,
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
}
