/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications

import com.mobilabsolutions.payment.notifications.configuration.SecurityConfig
import com.mobilabsolutions.payment.notifications.configuration.SwaggerConfiguration
import com.mobilabsolutions.server.commons.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(CommonConfiguration::class, SecurityConfig::class, SwaggerConfiguration::class)
@SpringBootApplication(
    exclude = [
        HttpMessageConvertersAutoConfiguration::class
    ]
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
