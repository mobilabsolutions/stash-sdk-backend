package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
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
import java.sql.Timestamp
import org.junit.jupiter.api.Assertions
import org.mockito.Spy

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HomeServiceTest {
    private val merchantId = "mobilab"
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val status = TransactionStatus.SUCCESS
    private val action = TransactionAction.REFUND
    private val paymentMethod = PaymentMethod.CC
    private val amount = 100
    private val currency = "EUR"
    private val correctTransactionId = "12345"
    private val createdAtStart = "2019-07-29T12:00:00Z"

    @Spy
    @InjectMocks
    private lateinit var homeService: HomeService

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
            Merchant(merchantId, pspConfig = pspConfig, timezone = "Europe/Berlin")
        )
        Mockito.`when`(transactionRepository.getTransactionsByFilters(merchantId, createdAtStart, null, null, action.name, status.name,
            null, 1000, 0))
            .thenReturn(listOf(arrayOf(correctTransactionId, amount, currency, status.name, action.name, "some reason", "some customer id", paymentMethod.name,
                Timestamp.valueOf("2019-07-29 13:00:00.000"), 1.toBigInteger())))
    }

    @Test
    fun `get refunds overview`() {
        Mockito.`when`(homeService.getPastDate(6)).thenReturn(createdAtStart)
        val refunds = homeService.getRefundsOverview(merchantId)

        Assertions.assertEquals(refunds.refunds[0].day, "Monday")
        Assertions.assertEquals(refunds.refunds[0].amount, 100)
    }
}
