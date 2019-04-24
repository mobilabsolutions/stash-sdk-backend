package com.mobilabsolutions.payment.adyen.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.model.PspConfigModel
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdyenPspTest {
    private val sandboxMerchantId = "some merchant id"
    private val sandboxPublicKey = "some public key"
    private val pspConfig = PspConfigModel(
        PaymentServiceProvider.ADYEN.toString(),
        null,
        null,
        null,
        null,
        sandboxMerchantId,
        sandboxPublicKey,
        null,
        null,
        null,
        true,
        null
    )

    @InjectMocks
    private lateinit var adyenPsp: AdyenPsp

    @Mock
    private lateinit var adyenClient: AdyenClient

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)
    }
}
