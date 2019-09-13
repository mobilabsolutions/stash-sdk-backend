/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.model.TransactionNotificationModel
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.pubsub.PgSubscriber
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.SynchronousQueue

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
        val vertx = Vertx.vertx()
        val subscriber = PgSubscriber.subscriber(vertx, connectionOptions).reconnectPolicy { 5000 }

        val queue = SynchronousQueue<AsyncResult<Void>>()
        subscriber.connect { result ->
            queue.put(result)
        }

        val connectionResult = queue.take()
        if (connectionResult.succeeded()) {
            logger.info { "Connection succeeded" }
            subscriber.channel(PgListener.POSTGRES_CHANNEL).handler { payload ->
                logger.info { "Listening to live data." }
                try {
                    val transactionNotification = objectMapper.readValue(payload, TransactionNotificationModel::class.java)
                    val merchantUsers = merchantUserRepository.getMerchantUsers(transactionNotification.merchantId!!)
                    merchantUsers.forEach { user ->
                        simpleMessagingTemplate.convertAndSendToUser(user.email!!, PgListener.TOPIC_NAME, homeService.toLiveData(transactionNotification.id!!))
                    }
                } catch (exception: Exception) {
                    logger.error("An error occurred while listening to live data: {}", exception.message)
                }
            }
        } else {
            vertx.close()
            throw connectionResult.cause()
        }
    }
}
