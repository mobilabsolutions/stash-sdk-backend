package com.mobilabsolutions.payment.auth.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Configuration
@EnableResourceServer
class ResourceServerConfiguration : ResourceServerConfigurerAdapter() {

    override fun configure(resources: ResourceServerSecurityConfigurer?) {
        resources!!.resourceId(RESOURCE_ID)
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
