package com.mobilabsolutions.payment.notifications.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Qualifier("userPasswordEncoder")
    @Autowired
    private lateinit var userPasswordEncoder: PasswordEncoder

    @Value("\${security.config.username:}")
    lateinit var username: String
    @Value("\${security.config.password:}")
    lateinit var password: String

    @Autowired
    fun configureGlobal(authentication: AuthenticationManagerBuilder) {
        authentication.inMemoryAuthentication()
            .passwordEncoder(userPasswordEncoder)
            .withUser(username)
            .password(userPasswordEncoder.encode(password))
            .authorities("USER")
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable().authorizeRequests()
            .antMatchers(*SWAGGER_PATTERNS, *PERMITTED_PATTERNS).permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
    }

    companion object {
        private val SWAGGER_PATTERNS = arrayOf("/swagger-ui.html", "/api-docs/**", "/webjars/**", "/v2/**", "/swagger-resources/**")
        private val PERMITTED_PATTERNS = arrayOf("/actuator/**")
    }
}
