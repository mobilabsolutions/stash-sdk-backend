package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.server.commons.CommonConfiguration
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
import java.sql.Timestamp
import java.util.Date

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionDetailsServiceTest {
    private val correctTransactionId = "12345"
    private val wrongTransactionId = "11111"
    private val correctAliasId = "correct alias id"
    private val pspAlias = "psp alias"
    private val merchantId = "mobilab"
    private val wrongMerchantId = "bad merchant"
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val pspResponse = "{\"pspTransactionId\":\"325105132\",\"status\":\"SUCCESS\",\"customerId\":\"160624370\"}"
    private val extra =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\", \"personalData\": {\"lastName\": \"Mustermann\",\"city\": \"Berlin\", \"country\": \"DE\"}}"
    private val paymentInfo = "{\"extra\":{\"personalData\":{\"email\":\"test@test.com\",\"customerIP\":\"89.1.82.51\",\"lastName\":\"Veskovic\",\"city\":\"Berlin\",\"country\":\"DE\"},\"paymentMethod\":\"CC\",\"payload\":\"Ab02b4c0!BQABAgBrnIFfbAPHjP4SAD0ZPREy1KiRqUJA6KGG3OGPt9xVG0GTajbsQX8vk/xZ+plfACWGsqC7n8eZxYkSQtc/PSMhxHcQUEdMUO7Oe7++pwwXLFPbd0i0dB8ezqCK7xx6UYdOp4hivuJJbMEvTWJ0wHE8MYemtSb0KEFPuvpCVWGhjfCpnA/jaIQMT4LuRnpChRW6RupEqkJk2GVVOXe/6RmT2UEed4Vk7cxSitf/PsxIiaEtlsEys6p0JD1vQh8OCJu+GraoW8D3rzQTbZHj+Bt2NZqmHpOy59Ns553kP1aS4p92aUgcI/7adkHwJlTxrnPt9dazBNxP8gXtf0c/adyhZ1pUkE7DNvTqGAqmd8Zb2hdJ9GYIYt3R11G1f8XY6TS4icthT6isPIeWZFApTxIoaPydPKSf7WHaw0TtLn+S3NlL/j8p3EBcPO6pwnm4aLDpQpkI3vOE20yes5XRT93ksHcqQyEYy3avwQpIqU6gcm4gZht9hjczSMK7FOujBHPgVZCwVPSXHbvDslfvF3l3yo2pNlkndhbfnRwbHBLlDWtyaFLwmWajEHkOSflPhk4UBc+OqJ3hudzpJy+RYuZpkND7CVibzyWDApLsN4LUl2yIqPtLIyc9bG4/0y6823uv7SlSh3XypIPnMyuXp7B7qlri0RY/3BXmbTivxH1sHxBczSVztuxzkUWzrIZIk7qMAEp7ImtleSI6IkFGMEFBQTEwM0NBNTM3RUFFRDg3QzI0REQ1MzkwOUI4MEE3OEE5MjNFMzgyM0Q2OERBQ0M5NEI5RkY4MzA1REMifbF4rsv1XjfAGsriuZwMi2ymLrfxMhq/aOPJWoDI2/sfowHpWpGvaz5rmfzuJ+MLa+AdaIih3d3rc+KacgOqvyRsOy9u9koYOGZ4LyUy29uIKOxJ8ln05pEcqIQ3ZKFFALQdcKwMLBHa6YmaDh6gqS/eaQ2BwT8l8cCBPIPZKX/zHBt9Ua3M8FLyIbkKcZe/4tDa8vgoG07BCKTRo104yHp599g3JARbY1wlRL27vEwPtP9wVeevf8PF56qN+8JAoHbUZIrw4AXwrwJy1tT6kkmBPvWM5D6nJec/5LoRZcGWMsTRl1GWNN95DezrMUOLsGI8IhQNFQazDlCOoVYQxizzmSzMmX0rrx7ynsOdfxTUhRUoBXJCHK8+bBTGgKZegub5JJyi87yiWy5Mug==\"},\"pspConfig\":{\"psp\":[{\"type\":\"BS_PAYONE\",\"merchantId\":\"42865\",\"portalId\":\"2030968\",\"key\":\"41P13T71t40B8F8f\",\"accountId\":\"42949\",\"default\":false},{\"type\":\"BRAINTREE\",\"sandboxMerchantId\":\"3zg84f628y5td5jy\",\"sandboxPublicKey\":\"p4n28yk7fjqvdv6y\",\"sandboxPrivateKey\":\"0ddaf018ea14dd2211c77d0f20a15d5b\",\"default\":false},{\"type\":\"ADYEN\",\"sandboxMerchantId\":\"MobilabSolutionsGmbHCOM\",\"sandboxPublicKey\":\"AQEvhmfuXNWTK0Qc+iSdnWYxq+WZe4RBGIdDV2tF4XWptmUJF0ekWxGCOMqMyvGxdyIQwV1bDb7kfNy1WIxIIkxgBw==-/MmC/tMOJx8t2FsKezDHBYyB9e73SBUtYk0oSC+fT1o=-7LzJDCZLmZNg98BT\",\"default\":true,\"currency\":\"EUR\",\"country\":\"DE\",\"locale\":\"de-DE\",\"urlPrefix\":\"random-mobilab\",\"sandboxUsername\":\"ws@Company.MobilabSolutionsGmbH\",\"sandboxPassword\":\"4nme^EmR3GVg%v)4+=Tt%w[>*\"}]}}"
    private val limit = 10
    private val offset = 0
    private val amount = 100
    private val currency = "EUR"
    private val status = TransactionStatus.SUCCESS
    private val action = TransactionAction.PREAUTH
    private val paymentMethod = PaymentMethod.CC
    private val transaction = Transaction(
        amount = amount,
        currencyId = currency,
        transactionId = correctTransactionId,
        paymentInfo = paymentInfo,
        pspTestMode = true,
        action = action,
        status = status,
        paymentMethod = paymentMethod,
        merchant = Merchant(merchantId, pspConfig = pspConfig),
        alias = Alias(id = correctAliasId, active = true, extra = extra, psp = PaymentServiceProvider.BS_PAYONE, pspAlias = pspAlias, merchant = Merchant("1", pspConfig = pspConfig)),
        pspResponse = pspResponse)

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @InjectMocks
    private lateinit var transactionDetailsService: TransactionDetailsService

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(merchantRepository.getMerchantById(merchantId)).thenReturn(
            Merchant(merchantId, pspConfig = pspConfig)
        )
        Mockito.`when`(transactionRepository.getByTransactionId(correctTransactionId)).thenReturn(transaction)
        Mockito.`when`(transactionRepository.getByTransactionId(wrongTransactionId)).thenReturn(null)
        Mockito.`when`(transactionRepository.getTransactionsByLimitAndOffset(merchantId, limit, offset)).thenReturn(
            listOf(arrayOf(correctTransactionId, amount, currency, status.name, action.name, "some reason", "some customer id", paymentMethod.name, Timestamp(
                Date().time
            ))))
    }

    @Test
    fun `get transaction by id successfully`() {
        val transaction = transactionDetailsService.getTransaction(merchantId, correctTransactionId)
        Assertions.assertNotNull(transaction)
    }

    @Test
    fun `get transaction by id unsuccessfully`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionDetailsService.getTransaction(merchantId, wrongTransactionId)
        }
    }

    @Test
    fun `get transactions successfully`() {
        val transactionList = transactionDetailsService.getTransactions(merchantId, limit, offset)
        Assertions.assertEquals(transactionList.transactions.size, 1)
    }

    @Test
    fun `get transactions successfully with default limit and offset`() {
        val transactionList = transactionDetailsService.getTransactions(merchantId, null, null)
        Assertions.assertEquals(transactionList.transactions.size, 1)
    }

    @Test
    fun `get transactions unsuccessfully`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionDetailsService.getTransactions(wrongMerchantId, null, null)
        }
    }
}
