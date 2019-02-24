package com.mobilabsolutions.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentSdkBackendApplication

fun main(args: Array<String>) {
	runApplication<PaymentSdkBackendApplication>(*args)
}
