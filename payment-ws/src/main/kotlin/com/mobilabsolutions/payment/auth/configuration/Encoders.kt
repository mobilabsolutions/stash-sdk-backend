package com.mobilabsolutions.payment.auth.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class Encoders {

    @Bean
    fun oauthClientPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(4)
    }

    @Bean
    @Primary
    fun userPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(8)
    }
}