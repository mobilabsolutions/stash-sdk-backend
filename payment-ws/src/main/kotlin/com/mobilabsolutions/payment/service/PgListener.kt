/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPoolOptions
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
        const val MERCHANT_ID = "merchant_id"
        const val TRANSACTION_ID = "id"
    }

    init {
        val options = PgPoolOptions()
            .setPort(port.toInt())
            .setHost(host)
            .setDatabase(database)
            .setUser(username)
            .setPassword(password)
        val client = PgClient.pool(options)
        client.getConnection { ar1 ->
            if (ar1.succeeded()) {
                val connection = ar1.result()

                connection.notificationHandler { notification ->
                    run {
                        logger.info { "Listening to live data." }
                        val transactionNotification = JSONObject(notification.payload)
                        val merchantUsers = merchantUserRepository.getMerchantUsers(transactionNotification.getString(MERCHANT_ID))
                        merchantUsers.forEach { user ->
                            simpleMessagingTemplate.convertAndSendToUser(user.email, "/topic/transactions",
                                homeService.toLiveData(transactionNotification.getLong(TRANSACTION_ID)))
                        }
                    }
                }

                connection.query("LISTEN transaction_record") { logger.info { "Subscribed to the channel." } }
            }
        }
    }
}
