/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.Alias
import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.MerchantApiKey
import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.enum.KeyType
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.MerchantApiKeyRepository
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspNotificationModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.model.request.PaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspCaptureRequestModel
import com.mobilabsolutions.payment.model.request.PspNotificationListRequestModel
import com.mobilabsolutions.payment.model.request.PspPaymentRequestModel
import com.mobilabsolutions.payment.model.request.PspRefundRequestModel
import com.mobilabsolutions.payment.model.request.PspReversalRequestModel
import com.mobilabsolutions.payment.model.request.ReversalRequestModel
import com.mobilabsolutions.payment.model.response.PspPaymentResponseModel
import com.mobilabsolutions.server.commons.CommonConfiguration
import com.mobilabsolutions.server.commons.exception.ApiException
import com.mobilabsolutions.server.commons.util.RequestHashing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.util.ReflectionTestUtils

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionServiceTest {
    private val newIdempotentKey = "new key"
    private val usedIdempotentKey = "used key"
    private val correctSecretKey = "correct key"
    private val someSecretKey = "some secret key"
    private val wrongSecretKey = "wrong key"
    private val correctMerchantId = "merchant id"
    private val wrongMerchantId = "wrong id"
    private val correctAliasId = "correct alias id"
    private val wrongAliasId = "wrong alias id"
    private val purchaseId = "purchase id"
    private val customerId = "customer id"
    private val pspTransactionId = "325105132"
    private val correctTransactionId = "correct transaction id"
    private val wrongTransactionId = "wrong transaction id"
    private val correctTransactionIdWithoutAuth = "correct transaction id without auth"
    private val correctTransactionIdAlreadyCaptured = "already captured transaction"
    private val correctTransactionIdAlreadyReversed = "already reversed transaction"
    private val correctTransactionIdWrongTestMode = "prod transaction"
    private val pspAlias = "psp alias"
    private val test = true
    private val preauthAction = TransactionAction.PREAUTH
    private val authAction = TransactionAction.AUTH
    private val correctPaymentData = PaymentDataRequestModel(1, "EUR", "reason")
    private val wrongPaymentData = PaymentDataRequestModel(2, "EUR", "reason")
    private val notifApiKey = "test-key"
    private val kafkaTopicName = "test-topic"
    private val pspTransId = "325105132"
    private val pspResponse = "{\"pspTransactionId\":\"$pspTransId\",\"status\":\"SUCCESS\",\"customerId\":\"160624370\"}"
    private val pspConfig = "{\"psp\" : [{\"type\" : \"BS_PAYONE\", \"portalId\" : \"123\", \"key\" : \"123\"," +
        " \"merchantId\" : \"mobilab\", \"accountId\" : \"123\", \"default\" : \"true\"}]}"
    private val extra =
        "{\"email\": \"test@test.com\",\"paymentMethod\": \"CC\", \"personalData\": {\"lastName\": \"Mustermann\",\"city\": \"Berlin\", \"country\": \"DE\"}}"
    private val reverseInfo = ReversalRequestModel("some reason")
    private val pspConfigModel = PspConfigModel(
        PaymentServiceProvider.BS_PAYONE.name, "mobilab", "123", "123", "123", null, null, null, null, null, true, null, null, null, null
    )
    private val aliasExtra = AliasExtraModel(null, null, null, PersonalDataModel(null, null, null, "Mustermann", null, null, "Berlin", "DE", null), PaymentMethod.CC.name, null)
    private val merchantTransactionId = "12345"
    private val requestHash = "17b7f62ccd46abba2576714496908760"
    private val differentRequestHash = "different hash"

    @InjectMocks
    private lateinit var transactionService: TransactionService

    private val transactionRepository = mock(TransactionRepository::class.java)

    @Mock
    private lateinit var merchantApiKeyRepository: MerchantApiKeyRepository

    @Mock
    private lateinit var aliasIdRepository: AliasRepository

    @Mock
    private lateinit var merchantRepository: MerchantRepository

    @Mock
    private lateinit var psp: Psp

    @Mock
    private lateinit var pspRegistry: PspRegistry

    @Mock
    private lateinit var requestHashing: RequestHashing

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(transactionService, "paymentApiKey", notifApiKey)
        ReflectionTestUtils.setField(transactionService, "kafkaTopicName", kafkaTopicName)

        Mockito.`when`(
            merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(
                true,
                KeyType.SECRET,
                correctSecretKey
            )
        ).thenReturn(
            MerchantApiKey(
                active = true,
                merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
            )
        )
        Mockito.`when`(
            merchantApiKeyRepository.getFirstByActiveAndKeyTypeAndKey(
                true,
                KeyType.SECRET,
                someSecretKey
            )
        ).thenReturn(
            MerchantApiKey(
                active = true,
                merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
            )
        )
        Mockito.`when`(merchantRepository.getMerchantById(correctMerchantId)).thenReturn(
            Merchant(
                correctMerchantId,
                pspConfig = pspConfig
            )
        )
        Mockito.`when`(merchantRepository.getMerchantById(wrongMerchantId)).thenReturn(null)
        Mockito.`when`(aliasIdRepository.getFirstByIdAndActive(correctAliasId, true)).thenReturn(
            Alias(
                id = correctAliasId,
                active = true,
                extra = extra,
                psp = PaymentServiceProvider.BS_PAYONE,
                pspAlias = pspAlias,
                merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
            )
        )
        Mockito.`when`(pspRegistry.find(PaymentServiceProvider.BS_PAYONE)).thenReturn(psp)
        Mockito.`when`(
            psp.preauthorize(
                PspPaymentRequestModel(correctAliasId, aliasExtra, correctPaymentData, pspAlias, pspConfigModel, purchaseId),
                test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(
            psp.authorize(
                PspPaymentRequestModel(correctAliasId, aliasExtra, correctPaymentData, pspAlias, pspConfigModel, purchaseId),
                test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(
            psp.capture(PspCaptureRequestModel(pspTransactionId, 1, "EUR", pspConfigModel, merchantTransactionId), test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(
            psp.reverse(PspReversalRequestModel(pspTransactionId, "EUR", pspConfigModel, merchantTransactionId), test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(
            psp.refund(PspRefundRequestModel(pspTransactionId, 1, "EUR", TransactionAction.AUTH.name, pspConfigModel, null, PaymentMethod.CC.name), test
            )
        ).thenReturn(PspPaymentResponseModel(pspTransactionId, TransactionStatus.SUCCESS, customerId, null, null))
        Mockito.`when`(
            transactionRepository.getByIdempotentKeyAndMerchant(
                newIdempotentKey,
                merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
            ))
            .thenReturn(null)
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndMerchant(
            usedIdempotentKey,
            merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
        ))
            .thenReturn(
                Transaction(
                    amount = 1,
                    currencyId = "EUR",
                    transactionId = correctTransactionId,
                    pspTestMode = test,
                    action = preauthAction,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                    paymentMethod = PaymentMethod.CC,
                    requestHash = requestHash,
                    alias = Alias(
                        id = correctAliasId,
                        active = true,
                        extra = extra,
                        psp = PaymentServiceProvider.BS_PAYONE,
                        pspAlias = pspAlias,
                        merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                    ),
                    pspResponse = pspResponse
                )
            )
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndMerchant(
            newIdempotentKey,
            merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
        ))
            .thenReturn(null)
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndMerchant(
            usedIdempotentKey,
            merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
        ))
            .thenReturn(
                Transaction(
                    amount = 1,
                    currencyId = "EUR",
                    transactionId = correctTransactionId,
                    pspTestMode = test,
                    action = authAction,
                    paymentMethod = PaymentMethod.CC,
                    requestHash = requestHash,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                    alias = Alias(
                        id = correctAliasId,
                        active = true,
                        extra = extra,
                        psp = PaymentServiceProvider.BS_PAYONE,
                        pspAlias = pspAlias,
                        merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                    ),
                    pspResponse = pspResponse
                )
            )
        Mockito.`when`(transactionRepository.getByIdempotentKeyAndMerchant(
            usedIdempotentKey,
            merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
        ))
            .thenReturn(
                Transaction(
                    amount = 1,
                    currencyId = "EUR",
                    transactionId = correctTransactionId,
                    pspTestMode = test,
                    action = TransactionAction.REFUND,
                    paymentMethod = PaymentMethod.CC,
                    requestHash = requestHash,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                    alias = Alias(
                        id = correctAliasId,
                        active = true,
                        extra = extra,
                        psp = PaymentServiceProvider.BS_PAYONE,
                        pspAlias = pspAlias,
                        merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                    ),
                    pspResponse = pspResponse
                )
            )
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndStatus(
                correctTransactionId,
                TransactionStatus.SUCCESS.toString()
            )
        ).thenReturn(
            Transaction(
                amount = 1,
                currencyId = "EUR",
                transactionId = correctTransactionId,
                pspTestMode = test,
                action = TransactionAction.PREAUTH,
                paymentMethod = PaymentMethod.CC,
                requestHash = requestHash,
                merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                alias = Alias(
                    id = correctAliasId,
                    active = true,
                    extra = extra,
                    psp = PaymentServiceProvider.BS_PAYONE,
                    pspAlias = pspAlias,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                ),
                pspResponse = pspResponse,
                merchantTransactionId = merchantTransactionId
            )
        )
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndStatus(
                correctTransactionIdAlreadyCaptured,
                TransactionStatus.SUCCESS.toString()
            )
        ).thenReturn(
            Transaction(
                amount = 1,
                currencyId = "EUR",
                transactionId = correctTransactionId,
                pspTestMode = test,
                action = TransactionAction.CAPTURE,
                paymentMethod = PaymentMethod.CC,
                requestHash = requestHash,
                merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                alias = Alias(
                    id = correctAliasId,
                    active = true,
                    extra = extra,
                    psp = PaymentServiceProvider.BS_PAYONE,
                    pspAlias = pspAlias,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                ),
                pspResponse = pspResponse,
                merchantTransactionId = merchantTransactionId
            )
        )
        Mockito.`when`(
            transactionRepository.getByTransactionIdAndStatus(
                correctTransactionIdAlreadyReversed,
                TransactionStatus.SUCCESS.toString()
            )
        ).thenReturn(
            Transaction(
                amount = 1,
                currencyId = "EUR",
                transactionId = correctTransactionId,
                pspTestMode = test,
                action = TransactionAction.REVERSAL,
                paymentMethod = PaymentMethod.CC,
                requestHash = requestHash,
                merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                alias = Alias(
                    id = correctAliasId,
                    active = true,
                    extra = extra,
                    psp = PaymentServiceProvider.BS_PAYONE,
                    pspAlias = pspAlias,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                ),
                pspResponse = pspResponse,
                merchantTransactionId = merchantTransactionId
            )
        )
        Mockito.`when`(
            transactionRepository.getListByTransactionIdAndStatus(correctTransactionId, TransactionStatus.SUCCESS.name)
        ).thenReturn(
            mutableListOf(
                Transaction(
                    amount = 50,
                    transactionId = correctTransactionId,
                    currencyId = "EUR",
                    pspTestMode = test,
                    action = TransactionAction.AUTH,
                    paymentMethod = PaymentMethod.CC,
                    requestHash = requestHash,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                    alias = Alias(
                        id = correctAliasId,
                        active = true,
                        extra = extra,
                        psp = PaymentServiceProvider.BS_PAYONE,
                        pspAlias = pspAlias,
                        merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                    ),
                    pspResponse = pspResponse
                ), Transaction(
                    amount = 1,
                    transactionId = correctTransactionId,
                    currencyId = "EUR",
                    pspTestMode = test,
                    action = TransactionAction.REFUND,
                    paymentMethod = PaymentMethod.CC,
                    requestHash = requestHash,
                    merchant = Merchant(correctMerchantId, pspConfig = pspConfig),
                    alias = Alias(
                        id = correctAliasId,
                        active = true,
                        extra = extra,
                        psp = PaymentServiceProvider.BS_PAYONE,
                        pspAlias = pspAlias,
                        merchant = Merchant(correctMerchantId, pspConfig = pspConfig)
                    ),
                    pspResponse = pspResponse
                )
            )
        )
        Mockito.`when`(requestHashing.hashRequest(PaymentRequestModel(correctAliasId, correctPaymentData, purchaseId, customerId))
        ).thenReturn(requestHash)
        Mockito.`when`(requestHashing.hashRequest(PaymentRequestModel(correctAliasId, correctPaymentData, "2", customerId))
        ).thenReturn(differentRequestHash)
        Mockito.`when`(requestHashing.hashRequest(PaymentRequestModel(correctAliasId, correctPaymentData, null, null))
        ).thenReturn(requestHash)
    }

    @Test
    fun `create auth transaction notification successfully`() {
        transactionService.createNotificationTransactionRecord(
            PspNotificationListRequestModel(
                notifications = mutableListOf(
                    PspNotificationModel(
                        pspTransactionId = pspTransId,
                        paymentData = PaymentDataRequestModel(
                            amount = 2,
                            currency = "EUR",
                            reason = "test"
                        ),
                        transactionAction = TransactionAction.AUTH.name,
                        transactionStatus = TransactionStatus.SUCCESS.name
                    )
                )
            ), notifApiKey
        )
        Mockito.verify(transactionRepository, times(1)).getByPspReferenceAndActions(pspTransId, TransactionAction.AUTH.name, TransactionAction.PREAUTH.name)
    }

    @Test
    fun `create capture transaction notification successfully`() {
        transactionService.createNotificationTransactionRecord(
            PspNotificationListRequestModel(
                notifications = mutableListOf(
                    PspNotificationModel(
                        pspTransactionId = pspTransId,
                        paymentData = PaymentDataRequestModel(
                            amount = 1,
                            currency = "EUR",
                            reason = "test"
                        ),
                        transactionAction = TransactionAction.CAPTURE.name,
                        transactionStatus = TransactionStatus.SUCCESS.name
                    )
                )
            ), notifApiKey
        )
        Mockito.verify(transactionRepository, times(1)).getByPspReferenceAndActions(pspTransId, TransactionAction.CAPTURE.name, TransactionAction.CAPTURE.name)
    }

    @Test
    fun `create auth transaction notification with wrong api key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.createNotificationTransactionRecord(
                PspNotificationListRequestModel(
                    notifications = mutableListOf(
                        PspNotificationModel(
                            pspTransactionId = pspTransId,
                            paymentData = PaymentDataRequestModel(
                                amount = 1,
                                currency = "EUR",
                                reason = "test"
                            ),
                            transactionAction = TransactionAction.AUTH.name,
                            transactionStatus = TransactionStatus.SUCCESS.name
                        )
                    )
                ), "bad api key"
            )
        }
    }

    @Test
    fun `preauthorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                wrongSecretKey,
                usedIdempotentKey,
                test,
                Mockito.mock(PaymentRequestModel::class.java)
            )
        }
    }

    @Test
    fun `preauthorize transaction with correct secret key`() {
        transactionService.preauthorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `preauthorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(
                    wrongAliasId,
                    correctPaymentData,
                    purchaseId,
                    customerId
                )
            )
        }
    }

    @Test
    fun `preauthorize transaction with correct alias id`() {
        transactionService.preauthorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `preauthorize transaction with used payment data`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(
                    wrongAliasId,
                    wrongPaymentData,
                    purchaseId,
                    customerId
                )
            )
        }
    }

    @Test
    fun `preauthorize transaction with new idempotent key`() {
        transactionService.preauthorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `preauthorize transaction with used idempotent key`() {
        transactionService.preauthorize(
            correctSecretKey,
            usedIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `preauthorize transaction with used idempotent key and different request body`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                usedIdempotentKey,
                test,
                PaymentRequestModel(
                    correctAliasId,
                    correctPaymentData,
                    "2",
                    customerId
                )
            )
        }
    }

    @Test
    fun `authorize transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                wrongSecretKey,
                usedIdempotentKey,
                test,
                Mockito.mock(PaymentRequestModel::class.java)
            )
        }
    }

    @Test
    fun `authorize transaction with correct secret key`() {
        transactionService.authorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `authorize transaction with wrong alias id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(
                    wrongAliasId,
                    correctPaymentData,
                    purchaseId,
                    customerId
                )
            )
        }
    }

    @Test
    fun `authorize transaction with correct alias id`() {
        transactionService.authorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `authorize transaction with used payment data`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                newIdempotentKey,
                test,
                PaymentRequestModel(
                    wrongAliasId,
                    wrongPaymentData,
                    purchaseId,
                    customerId
                )
            )
        }
    }

    @Test
    fun `authorize transaction with new idempotent key`() {
        transactionService.authorize(
            correctSecretKey,
            newIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `authorize transaction with used idempotent key`() {
        transactionService.preauthorize(
            correctSecretKey,
            usedIdempotentKey,
            test,
            PaymentRequestModel(
                correctAliasId,
                correctPaymentData,
                purchaseId,
                customerId
            )
        )
    }

    @Test
    fun `authorize transaction with used idempotent key and different request body`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.preauthorize(
                correctSecretKey,
                usedIdempotentKey,
                test,
                PaymentRequestModel(
                    correctAliasId,
                    correctPaymentData,
                    "2",
                    customerId
                )
            )
        }
    }

    @Test
    fun `capture transaction with wrong transaction id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(correctSecretKey, test, wrongTransactionId)
        }
    }

    @Test
    fun `capture transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(wrongSecretKey, test, correctTransactionId)
        }
    }

    @Test
    fun `capture transaction successfully`() {
        transactionService.capture(correctSecretKey, test, correctTransactionId)
    }

    @Test
    fun `capture transaction without prior transaction of type auth`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(correctSecretKey, test, correctTransactionIdWithoutAuth)
        }
    }

    @Test
    fun `capture transaction that already has been captured`() {
        transactionService.capture(correctSecretKey, test, correctTransactionIdAlreadyCaptured)
    }

    @Test
    fun `capture transaction with wrong test mode`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.capture(correctSecretKey, test, correctTransactionIdWrongTestMode)
        }
    }

    @Test
    fun `reverse transaction with wrong transaction id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.reverse(correctSecretKey, test, wrongTransactionId, reverseInfo)
        }
    }

    @Test
    fun `reverse transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.reverse(wrongSecretKey, test, wrongTransactionId, reverseInfo)
        }
    }

    @Test
    fun `reverse transaction successfully`() {
        transactionService.reverse(correctSecretKey, test, correctTransactionId, reverseInfo)
    }

    @Test
    fun `reverse transaction without prior transaction of type auth`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.reverse(correctSecretKey, test, correctTransactionIdWithoutAuth, reverseInfo)
        }
    }

    @Test
    fun `reverse transaction that already has been reversed`() {
        transactionService.reverse(correctSecretKey, test, correctTransactionIdAlreadyReversed, reverseInfo)
    }

    @Test
    fun `reverse transaction with wrong test mode`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.reverse(correctSecretKey, test, correctTransactionIdWrongTestMode, reverseInfo)
        }
    }

    @Test
    fun `refund transaction with wrong secret key`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.refund(
                wrongSecretKey,
                usedIdempotentKey,
                test,
                correctTransactionId,
                Mockito.mock(PaymentDataRequestModel::class.java)
            )
        }
    }

    @Test
    fun `refund transaction with correct secret key`() {
        transactionService.refund(
            correctSecretKey,
            newIdempotentKey,
            test,
            correctTransactionId,
            correctPaymentData
        )
    }

    @Test
    fun `refund transaction with new idempotent key`() {
        transactionService.refund(
            correctSecretKey,
            newIdempotentKey,
            test,
            correctTransactionId,
            correctPaymentData
        )
    }

    @Test
    fun `refund transaction with used idempotent key`() {
        transactionService.refund(
            correctSecretKey,
            usedIdempotentKey,
            test,
            correctTransactionId,
            correctPaymentData
        )
    }

    @Test
    fun `refund transaction with used idempotent key and different request body`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.refund(
                correctSecretKey,
                usedIdempotentKey,
                test,
                correctTransactionId,
                wrongPaymentData
            )
        }
    }

    @Test
    fun `refund transaction successfully`() {
        transactionService.refund(
            someSecretKey,
            newIdempotentKey,
            test,
            correctTransactionId,
            correctPaymentData
        )
    }

    @Test
    fun `capture transaction from dashboard successfully`() {
        transactionService.dashboardCapture(correctMerchantId, test, correctTransactionId)
    }

    @Test
    fun `capture transaction from dashboard with wrong merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.dashboardCapture(wrongMerchantId, test, correctTransactionId)
        }
    }

    @Test
    fun `reverse transaction from dashboard successfully`() {
        transactionService.dashboardReverse(correctMerchantId, test, correctTransactionId, reverseInfo)
    }

    @Test
    fun `reverse transaction from dashboard with wrong merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.dashboardReverse(wrongMerchantId, test, correctTransactionId, reverseInfo)
        }
    }

    @Test
    fun `refund transaction from dashboard successfully`() {
        transactionService.dashboardRefund(correctMerchantId, newIdempotentKey, test, correctTransactionId, correctPaymentData)
    }

    @Test
    fun `refund transaction from dashboard with wrong merchant id`() {
        Assertions.assertThrows(ApiException::class.java) {
            transactionService.dashboardRefund(wrongMerchantId, newIdempotentKey, test, correctTransactionId, correctPaymentData)
        }
    }
}
