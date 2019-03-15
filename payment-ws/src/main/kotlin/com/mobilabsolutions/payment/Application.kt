package com.mobilabsolutions.payment

import com.mobilabsolutions.payment.auth.configuration.AuthServerOAuth2Config
import com.mobilabsolutions.payment.auth.configuration.ResourceServerConfiguration
import com.mobilabsolutions.payment.data.configuration.DataConfiguration
import com.mobilabsolutions.server.commons.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(DataConfiguration::class, CommonConfiguration::class, AuthServerOAuth2Config::class, ResourceServerConfiguration::class)
@SpringBootApplication(
    exclude = [
        HttpMessageConvertersAutoConfiguration::class
    ]
)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
