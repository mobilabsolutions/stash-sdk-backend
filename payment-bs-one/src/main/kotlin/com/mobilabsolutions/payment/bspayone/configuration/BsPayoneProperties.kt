package com.mobilabsolutions.payment.bspayone.configuration

import org.springframework.context.annotation.Configuration

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Configuration
class BsPayoneProperties {
    val apiVersion = "3.11"
    val encoding = "UTF-8"
    val baseUrl = "https://api.pay1.de/post-gateway/"
}