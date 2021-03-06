/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.server.commons

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Configuration
class Encoders {

    @Bean(name = ["clientPasswordEncoder"])
    fun oauthClientPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(4)
    }

    @Bean(name = ["userPasswordEncoder"])
    fun userPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(8)
    }
}
