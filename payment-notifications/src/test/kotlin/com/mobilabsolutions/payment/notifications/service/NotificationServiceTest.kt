/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.adyen.model.AdyenNotificationItemListModel
import com.mobilabsolutions.payment.adyen.model.AdyenNotificationItemModel
import com.mobilabsolutions.payment.adyen.model.request.AdyenNotificationRequestModel
import com.mobilabsolutions.payment.data.enum.NotificationStatus
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.model.PspNotificationModel
import com.mobilabsolutions.payment.model.request.PaymentDataRequestModel
import com.mobilabsolutions.payment.notifications.data.Notification
import com.mobilabsolutions.payment.notifications.data.NotificationId
import com.mobilabsolutions.payment.notifications.data.repository.NotificationRepository
import com.mobilabsolutions.payment.service.Psp
import com.mobilabsolutions.server.commons.configuration.CommonConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationServiceTest {

    @InjectMocks
    private lateinit var notificationService: NotificationService

    private val notificationRepository = mock(NotificationRepository::class.java)

    private val notificationClient = mock(NotificationClient::class.java)

    @Mock
    private lateinit var pspRegistry: PspRegistry

    @Mock
    private lateinit var psp: Psp

    @Spy
    val objectMapper: ObjectMapper = CommonConfiguration().jsonMapper()

    private val succesAdyenNotification = AdyenNotificationItemModel(
        additionalData = null,
        amount = null,
        pspReference = "1234",
        eventCode = "AUTHORISATION",
        eventDate = null,
        merchantAccountCode = null,
        operations = mutableListOf(),
        merchantReference = null,
        originalReference = null,
        paymentMethod = null,
        reason = null,
        success = "true"
    )

    private val falseAdyenNotification = AdyenNotificationItemModel(
        additionalData = null,
        amount = null,
        pspReference = "123",
        eventCode = "CAPTURE",
        eventDate = null,
        merchantAccountCode = null,
        operations = mutableListOf(),
        merchantReference = null,
        originalReference = null,
        paymentMethod = null,
        reason = null,
        success = "false"
    )

    private val paymentURL = "test-url.com"
    private val paymentApiKey = "test-api-key"
    private val successAdyenNotificationEntity = AdyenNotificationItemListModel(succesAdyenNotification).toNotification(PaymentServiceProvider.ADYEN)
    private val falseAdyenNotificationEntity = AdyenNotificationItemListModel(falseAdyenNotification).toNotification(PaymentServiceProvider.ADYEN)
    private val adyenNotificationRequestModel = AdyenNotificationRequestModel(
        live = "false",
        notificationItems = mutableListOf(AdyenNotificationItemListModel(succesAdyenNotification), AdyenNotificationItemListModel(falseAdyenNotification))
    )
    private val notificationModels = mutableListOf(
        PspNotificationModel(
            pspTransactionId = "1234",
            paymentData = PaymentDataRequestModel(
                amount = 1,
                currency = "EUR",
                reason = "test"
            ),
            transactionAction = TransactionAction.AUTH.name,
            transactionStatus = TransactionStatus.SUCCESS.name
        ),
        PspNotificationModel(
            pspTransactionId = "123",
            paymentData = PaymentDataRequestModel(
                amount = 1,
                currency = "EUR",
                reason = "test"
            ),
            transactionAction = TransactionAction.CAPTURE.name,
            transactionStatus = TransactionStatus.SUCCESS.name
        )
    )

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(notificationService, "paymentURL", paymentURL)
        ReflectionTestUtils.setField(notificationService, "paymentApiKey", paymentApiKey)

        Mockito.`when`(pspRegistry.find(PaymentServiceProvider.ADYEN)).thenReturn(psp)
        Mockito.`when`(notificationRepository.findNotificationByPsp(PaymentServiceProvider.ADYEN.name, 2)).thenReturn(
            mutableListOf(successAdyenNotificationEntity, falseAdyenNotificationEntity)
        )
        Mockito.`when`(notificationClient.sendNotifications(paymentURL, paymentApiKey, notificationModels)).thenReturn(201)
    }

    @Test
    fun `accept and persist incoming Adyen transactions successfully`() {
        notificationService.saveAdyenNotifications(adyenNotificationRequestModel)
        verify(notificationRepository, times(2)).save(any(Notification::class.java))
        reset(notificationRepository)
    }

    @Test
    fun `process Adyen transactions successfully`() {
        notificationService.processNotifications(PaymentServiceProvider.ADYEN.name)
        verify(notificationClient, times(1)).sendNotifications(anyString(), anyString(), anyList())
        verify(notificationRepository, times(2)).save(any(Notification::class.java))
        reset(notificationRepository)
    }

    private fun AdyenNotificationItemListModel.toNotification(psp: PaymentServiceProvider): Notification {
        return Notification(
            notificationId = NotificationId(pspTransactionId = this.notificationRequestItem?.pspReference, pspEvent = this.notificationRequestItem?.eventCode),
            status = NotificationStatus.CREATED,
            psp = psp,
            message = objectMapper.writeValueAsString(this.notificationRequestItem)
        )
    }
}
