package com.mobilabsolutions.payment

import com.mobilabsolutions.payment.data.configuration.DataConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(DataConfiguration::class)
@SpringBootApplication
class PaymentSdkBackendApplication

fun main(args: Array<String>) {
    runApplication<PaymentSdkBackendApplication>(*args)
}
