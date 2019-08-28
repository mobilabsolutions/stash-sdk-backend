/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.model.LiveTransactionModel
import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPoolOptions
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * Listens to postgres notifications, and sends them via WebSocket to the client.
 *
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Component
class PgListener(
    private val merchantUserRepository: MerchantUserRepository,
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    private val homeService: HomeService,
    private val objectMapper: ObjectMapper
) {

    companion object : KLogging()

    @Value("\${postgres.db.port:}")
    private lateinit var port: String

    @Value("\${postgres.db.host:}")
    private lateinit var host: String

    @Value("\${postgres.db.name:}")
    private lateinit var database: String

    @Value("\${spring.datasource.username:}")
    private lateinit var username: String

    @Value("\${spring.datasource.password:}")
    private lateinit var password: String

    @PostConstruct
    fun startListening() {
        val options = PgPoolOptions()
            .setPort(port.toInt())
            .setHost(host)
            .setDatabase(database)
            .setUser(username)
            .setPassword(password)
        val client = PgClient.pool(options)
        client.getConnection { ar1 ->
            if (ar1.succeeded()) {
                println("CONNECTED")
                val connection = ar1.result()

                connection.notificationHandler { notification ->
                    run {
                        println("Received ${notification.payload}")
                        val transaction = objectMapper.readValue(notification.payload, LiveTransactionModel::class.java)
                        val merchantUsers = merchantUserRepository.getMerchantUsers(transaction.merchantId!!)
                        merchantUsers.forEach { user ->
                            simpleMessagingTemplate.convertAndSendToUser(user.email, "/topic/transactions", homeService.toLiveData(transaction, transaction.merchantId)!!)
                            println(homeService.toLiveData(transaction, transaction.merchantId))
                        }
                    }
                }

                connection.query("LISTEN transaction_record") { logger.info { "Subscribed to the channel." } }
            }
        }
    }
}
