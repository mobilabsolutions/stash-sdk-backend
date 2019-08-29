/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.pubsub.PgSubscriber
import mu.KLogging
import org.json.JSONObject
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
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    private val homeService: HomeService,
    @Value("\${postgres.db.port:}") private val port: String,
    @Value("\${postgres.db.host:}") private val host: String,
    @Value("\${postgres.db.name:}") private val database: String,
    @Value("\${spring.datasource.username:}") private val username: String,
    @Value("\${spring.datasource.password:}") private val password: String
) {

    companion object : KLogging() {
        private const val MERCHANT_ID = "merchant_id"
        private const val TRANSACTION_ID = "id"
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
                    println("Received $payload")
                    val transactionNotification = JSONObject(payload)
                    val merchantUsers = merchantUserRepository.getMerchantUsers(transactionNotification.getString(MERCHANT_ID))
                    merchantUsers.forEach { user ->
                        simpleMessagingTemplate.convertAndSendToUser(user.email, TOPIC_NAME, homeService.toLiveData(transactionNotification.getLong(TRANSACTION_ID)))
                    }
                }
            }
        }

        subscriber.reconnectPolicy { 5000 }
    }
}
