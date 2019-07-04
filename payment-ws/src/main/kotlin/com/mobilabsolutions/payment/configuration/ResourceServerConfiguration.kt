/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Configuration
@EnableResourceServer
class ResourceServerConfiguration(private val exceptionTranslator: CustomWebResponseExceptionTranslator) : ResourceServerConfigurerAdapter() {

    override fun configure(resources: ResourceServerSecurityConfigurer?) {
        val authenticationEntryPoint = OAuth2AuthenticationEntryPoint()
        authenticationEntryPoint.setExceptionTranslator(exceptionTranslator)
        resources!!.authenticationEntryPoint(authenticationEntryPoint)

        val accessDeniedHandler = OAuth2AccessDeniedHandler()
        accessDeniedHandler.setExceptionTranslator(exceptionTranslator)
        resources.accessDeniedHandler(accessDeniedHandler)

        resources.resourceId(RESOURCE_ID)
    }

    override fun configure(http: HttpSecurity) {
        http.requestMatchers().anyRequest().and().authorizeRequests()
            .antMatchers(*PERMITTED_PATTERNS, *SWAGGER_PATTERNS).permitAll()
            .anyRequest().access(USER_SCOPE)
    }

    companion object {
        private const val RESOURCE_ID = "payment-sdk-rest-api"
        private const val USER_SCOPE = "#oauth2.hasScope('user')"
        private val SWAGGER_PATTERNS = arrayOf("/swagger-ui.html", "/api-docs/**", "/webjars/**", "/v2/**", "/swagger-resources/**")
        private val PERMITTED_PATTERNS = arrayOf("/actuator/**", "/alias/**", "/preauthorization/**", "/authorization/**")
    }
}
