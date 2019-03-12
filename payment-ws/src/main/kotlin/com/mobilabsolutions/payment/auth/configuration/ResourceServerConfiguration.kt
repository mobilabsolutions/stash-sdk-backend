package com.mobilabsolutions.payment.auth.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer

@Configuration
@EnableResourceServer
class ResourceServerConfiguration : ResourceServerConfigurerAdapter() {

    override fun configure(resources: ResourceServerSecurityConfigurer?) {
        resources!!.resourceId(RESOURCE_ID)
    }

    override fun configure(http: HttpSecurity) {
        http.requestMatchers().anyRequest()
            .antMatchers(SECURED_PATTERN).and().authorizeRequests()
            .antMatchers(HttpMethod.POST, SECURED_PATTERN).access(SECURED_WRITE_SCOPE)
            .anyRequest().permitAll()
    }

    companion object {
        private const val RESOURCE_ID = "resource-server-rest-api"
        private const val SECURED_READ_SCOPE = "#oauth2.hasScope('read')"
        private const val SECURED_WRITE_SCOPE = "#oauth2.hasScope('write')"
        private const val SECURED_PATTERN = "/secured/**"
    }
}