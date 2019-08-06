package com.mobilabsolutions.payment.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Configuration
@EnableKafka
class KafkaConfiguration {

    @Bean
    fun newTopic(
        @Value("\${kafka.transactions.topicName:}") topicName: String
    ): NewTopic {
        return NewTopic(topicName, 1, 1)
    }
}
