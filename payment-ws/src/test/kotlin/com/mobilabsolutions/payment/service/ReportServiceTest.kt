package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Filter
import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.enum.ReportType
import com.mobilabsolutions.payment.data.repository.FilterRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.mock.web.MockHttpServletResponse

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportServiceTest {
    private val merchantId = "mobilab"
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val response = MockHttpServletResponse()
    private val incorrectMerchantId = "notMobilab"
    private val filterName = "filter"
    private val createdAtStart = "2019-07-29T00:00:00Z"
    private val createdAtEnd = "2019-07-29T23:59:59Z"
    private val paymentMethod = "PAY_PAL"
    private val status = "SUCCESS"
    private val currency = "EUR"
    private val amount = "1000"
    private val customerId = "123"
    private val transactionId = "123"
    private val merchantTransactionId = "123"

    @InjectMocks
    private lateinit var reportService: ReportService

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Mock
    private lateinit var filterRepository: FilterRepository

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
            Merchant(merchantId, pspConfig = pspConfig, timezone = "Europe/Berlin")
        )
        Mockito.`when`(filterRepository.getFilterById(filterName)).thenReturn(
            Filter(filterName, createdAtStart, createdAtEnd, status, paymentMethod, null)
        )
        Mockito.`when`(filterRepository.getFiltersByMerchantId(merchantId)).thenReturn(
            listOf(Filter(filterName, createdAtStart, createdAtEnd, status, paymentMethod, null))
        )
    }

    @Test
    fun `export default dashboard transactions to csv successfully`() {
        reportService.downloadDefaultReports(response, ReportType.OVERVIEW.name, merchantId)
    }

    @Test
    fun `export default dashboard transactions to csv with incorrect merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            reportService.downloadDefaultReports(response, ReportType.OVERVIEW.name, incorrectMerchantId)
        }
    }

    @Test
    fun `export custom dashboard transactions to csv successfully`() {
        reportService.downloadCustomReports(response, merchantId, filterName, createdAtStart, createdAtEnd, paymentMethod, status, null, currency, amount, customerId, transactionId, merchantTransactionId)
    }

    @Test
    fun `export custom dashboard transactions to csv with incorrect merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            reportService.downloadCustomReports(response, incorrectMerchantId, filterName, createdAtStart, createdAtEnd, paymentMethod, status, null, currency, amount, customerId, transactionId, merchantTransactionId)
        }
    }

    @Test
    fun `get all report filter names successfully`() {
        val filtersList = reportService.getAllReportFilters(merchantId)
        Assertions.assertEquals(filtersList.filters[0].filterName, filterName)
    }

    @Test
    fun `get all report filter names with incorrect merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            reportService.getAllReportFilters(incorrectMerchantId)
        }
    }
}
