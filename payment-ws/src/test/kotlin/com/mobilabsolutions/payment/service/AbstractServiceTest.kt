package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.service.psp.PspRegistry
import com.mobilabsolutions.server.commons.CommonConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
abstract class AbstractServiceTest {
    protected var merchant = Merchant()
    protected val merchantId = "test"
    protected val knownPspType = "BS_PAYONE"

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @InjectMocks
    protected lateinit var aliasService: AliasService

    @InjectMocks
    protected lateinit var merchantService: MerchantService

    @Mock
    protected lateinit var aliasRepository: AliasRepository

    @Mock
    protected lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    protected lateinit var merchantRepository: MerchantRepository

    @Mock
    protected lateinit var pspRegistry: PspRegistry

    @BeforeEach
    fun setUp() {
        val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"test portal\"}," +
            " {\"type\" : \"other\", \"merchantId\" : \"test merchant\"}]}"
        merchant = Merchant(id = merchantId, pspConfig = pspConfig)
    }
}