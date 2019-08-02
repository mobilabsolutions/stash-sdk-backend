package com.mobilabsolutions.payment

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class])
class TestConfiguration
