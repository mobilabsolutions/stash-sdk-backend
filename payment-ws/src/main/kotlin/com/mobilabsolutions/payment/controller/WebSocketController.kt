package com.mobilabsolutions.payment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.service.HomeService
import com.mobilabsolutions.payment.service.PgListener
import mu.KLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Controller
class WebSocketController(
    private val merchantUserRepository: MerchantUserRepository,
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    private val homeService: HomeService,
    private val pgListener: PgListener,
    private val objectMapper: ObjectMapper
) {

    companion object : KLogging()

    @RequestMapping("/send-data", method = [RequestMethod.POST])
    fun sendLiveData() {
        logger.info { "Started listening live data" }
        val notification = pgListener.listenNotifications()
        val transaction = objectMapper.readValue(notification, Transaction::class.java)
        val merchantUsers = merchantUserRepository.getMerchantUsers(transaction.merchant.id!!)
        merchantUsers.forEach { user ->
            simpleMessagingTemplate.convertAndSendToUser(user.email, "/topic/transactions", homeService.toLiveData(transaction))
            println(homeService.toLiveData(transaction))
        }
    }
}
