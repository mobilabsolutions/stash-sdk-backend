package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.enum.ReportType
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

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportServiceTest {
    private val merchantId = "mobilab"
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val response = MockHttpServletResponse()
    private val incorrectMerchantId = "notMobilab"

    @InjectMocks
    private lateinit var reportService: ReportService

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
            Merchant(merchantId, pspConfig = pspConfig, timezone = "Europe/Berlin")
        )
    }

    @Test
    fun `export dashboard transactions to csv successfully`() {
        reportService.downloadDefaultReports(response, ReportType.OVERVIEW.name, merchantId)
    }

    @Test
    fun `export dashboard transactions to csv with incorrect merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            reportService.downloadDefaultReports(response, ReportType.OVERVIEW.name, incorrectMerchantId)
        }
    }
}
