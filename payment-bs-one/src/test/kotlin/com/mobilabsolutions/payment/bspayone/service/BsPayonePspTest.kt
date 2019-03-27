package com.mobilabsolutions.payment.bspayone.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneClearingType
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneResponseStatus
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentResponseModel
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.model.PaymentDataModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PspConfigModel
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

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BsPayonePspTest {
    private val correctAliasId = "test"
    private val wrongAliasId = "wrong id"
    private val merchantConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val extra =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\", \"personalData\": {\"lastName\": \"Mustermann\",\"city\": \"Berlin\", \"country\": \"DE\"}}"
    private val reference = "Book"
    private val amount = 300
    private val currency = "EUR"
    private val customerId = "1"
    private val purchaseId = "1"
    private val pspTransactionId = "1123"
    private val accountId = "123"
    private val portalId = "123"
    private val key = "123"
    private val merchantId = "mobilab"
    private val lastName = "Mustermann"
    private val country = "DE"
    private val city = "Berlin"
    private val pspAlias = "1234"

    @InjectMocks
    private lateinit var bsPayonePsp: BsPayonePsp

    @Mock
    private lateinit var bsPayoneHashingService: BsPayoneHashingService

    @Mock
    private lateinit var bsPayoneClient: BsPayoneService

    @Mock
    private lateinit var aliasIdRepository: AliasRepository

    @Mock
    private lateinit var bsPayoneProperties: BsPayoneProperties

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        Mockito.`when`(aliasIdRepository.getFirstById(correctAliasId)).thenReturn(
            Alias(active = true, extra = extra, psp = PaymentServiceProvider.BS_PAYONE, pspAlias = pspAlias,
                merchant = Merchant(id = "1", pspConfig = merchantConfig))
        )

        Mockito.`when`(aliasIdRepository.getFirstById(wrongAliasId)).thenReturn(null)

        Mockito.`when`(bsPayoneClient.preauthorization(BsPayonePaymentRequestModel(accountId, BsPayoneClearingType.CC.type,
            reference, amount.toString(), currency, lastName, country, city, pspAlias, null, null),
            PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true)))
            .thenReturn(
            BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, pspTransactionId, customerId, null, null, null)
        )
    }

    @Test
    fun `preauthorize transaction with correct alias id`() {
        bsPayonePsp.preauthorize(PaymentRequestModel(correctAliasId, PaymentDataModel(amount, currency, reference), purchaseId, customerId), reference)
    }

    @Test
    fun `preauthorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.preauthorize(PaymentRequestModel(wrongAliasId, PaymentDataModel(amount, currency, reference), purchaseId, customerId), reference)
        }
    }

    @Test
    fun `calculate PSP config`() {
        bsPayonePsp.calculatePspConfig(PspConfigModel(PaymentServiceProvider.BS_PAYONE.toString(), merchantId, portalId, key, accountId, null, null, true))
    }
}