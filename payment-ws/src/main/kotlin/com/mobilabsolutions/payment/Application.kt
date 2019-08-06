/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment

import com.mobilabsolutions.payment.configuration.AuthServerOAuth2Config
import com.mobilabsolutions.payment.configuration.ResourceServerConfiguration
import com.mobilabsolutions.payment.configuration.SwaggerConfiguration
import com.mobilabsolutions.server.commons.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(CommonConfiguration::class, AuthServerOAuth2Config::class, ResourceServerConfiguration::class, SwaggerConfiguration::class)
@SpringBootApplication(
    exclude = [
        HttpMessageConvertersAutoConfiguration::class
    ]
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
