package com.mobilabsolutions.payment.bspayone.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneClearingType
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneResponseStatus
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentResponseModel
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PaymentDataModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PreauthorizeRequestModel
import com.mobilabsolutions.payment.model.PspConfigListModel
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
    private var pspConfig: PspConfigModel? = null

    @InjectMocks
    private lateinit var bsPayonePsp: BsPayonePsp

    @Mock
    private lateinit var bsPayoneHashingService: BsPayoneHashingService

    @Mock
    private lateinit var bsPayoneClient: BsPayoneClient

    @Mock
    private lateinit var aliasIdRepository: AliasRepository

    @Mock
    private lateinit var bsPayoneProperties: BsPayoneProperties

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)

        val merchantConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
            " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
        val config = objectMapper.readValue(merchantConfig, PspConfigListModel::class.java)
        pspConfig = config.psp.firstOrNull {it.type == PaymentServiceProvider.BS_PAYONE.toString()}
        val bsPayoneRequestModel = BsPayonePaymentRequestModel("123", BsPayoneClearingType.CC.type, "Book", "300", "EUR", "Mustermann", "DE", "Berlin", "1234", null, null)
        val extraModel = AliasExtraModel(null, null, null, PersonalDataModel("test@mblb.net", "Max", "Mustermann", null, null, "Berlin", "DE"), PaymentMethod.CC)
        val extra = objectMapper.writeValueAsString(extraModel)

        val merchant = Merchant(id = "mobilab", pspConfig = merchantConfig)

        Mockito.`when`(aliasIdRepository.getFirstById(correctAliasId)).thenReturn(
            Alias(active = true, extra = extra, psp = PaymentServiceProvider.BS_PAYONE, pspAlias = "1234", merchant = merchant)
        )

        Mockito.`when`(aliasIdRepository.getFirstById(wrongAliasId)).thenReturn(null)

        Mockito.`when`(bsPayoneClient.preauthorization(bsPayoneRequestModel, pspConfig!!)).thenReturn(
            BsPayonePaymentResponseModel(BsPayoneResponseStatus.APPROVED, "12345", "1", null, null, null)
        )
    }

    @Test
    fun `preauthorize transaction with correct alias id`() {
        bsPayonePsp.preauthorize(PreauthorizeRequestModel(correctAliasId, PaymentDataModel(300, "EUR", "Book"), "1", "1"))
    }

    @Test
    fun `preauthorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            bsPayonePsp.preauthorize(PreauthorizeRequestModel(wrongAliasId, PaymentDataModel(300, "EUR", "Book"), "1", "1"))
        }
    }

    @Test
    fun `calculate PSP config`() {
        bsPayonePsp.calculatePspConfig(pspConfig)
    }
}