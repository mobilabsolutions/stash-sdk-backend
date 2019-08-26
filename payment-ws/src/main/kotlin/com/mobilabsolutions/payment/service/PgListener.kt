package com.mobilabsolutions.payment.service

import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgPoolOptions
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Component
class PgListener {

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

    final var client: PgPool? = null

    init {
        val options = PgPoolOptions()
            .setPort(port.toInt())
            .setHost(host)
            .setDatabase(database)
            .setUser(username)
            .setPassword(password)
        client = PgClient.pool(options)
    }

    fun listenNotifications(): String? {
        var transaction: String? = null
        client?.getConnection { ar1 ->
            if (ar1.succeeded()) {
                println("CONNECTED")
                val connection = ar1.result()

                connection.notificationHandler { notification ->
                    run {
                        println("Received ${notification.payload}")
                        transaction = notification.payload
                    }
                }

                connection.query("LISTEN transaction_record") { logger.info { "Subscribed to channel" } }
            }
        }
        return transaction
    }
}
