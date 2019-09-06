/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.model.TransactionNotificationModel
import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.pubsub.PgSubscriber
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

/**
 * Listens to postgres notifications, and sends them via WebSocket to the client.
 *
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Component
class PgListener(
    private val merchantUserRepository: MerchantUserRepository,
    private val homeService: HomeService,
    private val objectMapper: ObjectMapper,
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    @Value("\${postgres.db.port:}") private val port: String,
    @Value("\${postgres.db.host:}") private val host: String,
    @Value("\${postgres.db.name:}") private val database: String,
    @Value("\${spring.datasource.username:}") private val username: String,
    @Value("\${spring.datasource.password:}") private val password: String
) {

    companion object : KLogging() {
        private const val POSTGRES_CHANNEL = "transaction_record"
        private const val TOPIC_NAME = "/topic/transactions"
    }

    init {
        val connectionOptions = PgConnectOptions()
            .setPort(port.toInt())
            .setHost(host)
            .setDatabase(database)
            .setUser(username)
            .setPassword(password)
        val subscriber = PgSubscriber.subscriber(Vertx.vertx(), connectionOptions)
        subscriber.connect { connection ->
            if (connection.succeeded()) {
                subscriber.channel(POSTGRES_CHANNEL).handler { payload ->
                    logger.info { "Listening to live data." }
                    try {
                        val transactionNotification = objectMapper.readValue(payload, TransactionNotificationModel::class.java)
                        val merchantUsers = merchantUserRepository.getMerchantUsers(transactionNotification.merchantId!!)
                        merchantUsers.forEach { user ->
                            simpleMessagingTemplate.convertAndSendToUser(user.email, TOPIC_NAME, homeService.toLiveData(transactionNotification.id!!))
                        }
                    } catch (exception: Exception) {
                        logger.error("An error occurred while listening to live data: {}", exception.message)
                    }
                }
            }
        }

        subscriber.reconnectPolicy { 5000 }
    }
}
