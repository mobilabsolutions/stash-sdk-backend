package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Alias
import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
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
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HomeServiceTest {
    private val merchantId = "mobilab"
    private val incorrectMerchantId = "NotMobilab"
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val status = TransactionStatus.SUCCESS
    private val action = TransactionAction.REFUND
    private val paymentMethod = PaymentMethod.CC
    private val amount = 100
    private val currency = "EUR"
    private val correctTransactionId = "12345"
    private val createdAtStart = "2019-07-29T12:00:00Z"
    private val merchant = Merchant(merchantId, pspConfig = pspConfig, timezone = "Europe/Berlin")
    private val transaction = Transaction(
        amount = amount,
        currencyId = currency,
        transactionId = correctTransactionId,
        paymentInfo = null,
        pspTestMode = true,
        action = action,
        status = status,
        paymentMethod = paymentMethod,
        merchant = merchant,
        alias = Alias(
            id = null,
            active = true,
            extra = null,
            psp = PaymentServiceProvider.BS_PAYONE,
            pspAlias = null,
            merchant = Merchant("1", pspConfig = pspConfig)
        ),
        pspResponse = null
    )

    @Spy
    @InjectMocks
    private lateinit var homeService: HomeService

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var merchantUserRepository: MerchantUserRepository

    @Mock
    private lateinit var simpleMessagingTemplate: SimpMessagingTemplate

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)
        transaction.createdDate = LocalDateTime.parse(createdAtStart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")).atZone(ZoneId.of("Europe/Berlin")).toInstant()
        Mockito.`when`(merchantRepository.getMerchantById(merchantId)).thenReturn(merchant)
        Mockito.`when`(merchantRepository.getMerchantById(incorrectMerchantId)).thenReturn(null)
        Mockito.`when`(transactionRepository.getTransactionsForRefunds(merchantId, createdAtStart, null))
            .thenReturn(listOf(transaction))
    }

    @Test
    fun `get refunds overview`() {
        Mockito.`when`(homeService.getPastDate(merchant, 6)).thenReturn(createdAtStart)
        val refunds = homeService.getRefundsOverview(merchantId)

        Assertions.assertEquals(refunds.refunds[0].day, "Monday")
        Assertions.assertEquals(refunds.refunds[0].amount, 100)
    }

    @Test
    fun `get refunds overview with incorrect merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            homeService.getRefundsOverview(incorrectMerchantId)
        }
    }

    @Test
    fun `get refunds overview with inaccurate start date`() {
        Mockito.`when`(homeService.getPastDate(merchant, 1)).thenReturn(createdAtStart)
        val refunds = homeService.getRefundsOverview(merchantId)

        Assertions.assertEquals(refunds.refunds.size, 0)
    }
}
