/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications

import com.mobilabsolutions.server.commons.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@Import(CommonConfiguration::class)
@EnableScheduling
@SpringBootApplication(
    exclude = [
        HttpMessageConvertersAutoConfiguration::class
    ]
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
