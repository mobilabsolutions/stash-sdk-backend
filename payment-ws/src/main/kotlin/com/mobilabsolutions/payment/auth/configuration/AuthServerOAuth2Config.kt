package com.mobilabsolutions.auth.config.server

import com.mobilabsolutions.payment.service.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import javax.sql.DataSource

@Configuration
@EnableAuthorizationServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import(ServerSecurityConfig::class)
class AuthServerOAuth2Config : AuthorizationServerConfigurerAdapter() {

    @Autowired
    @Qualifier("dataSource")
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Qualifier("clientPasswordEncoder")
    @Autowired
    private lateinit var oauthClientPasswordEncoder: PasswordEncoder

    @Bean
    fun tokenStore(): TokenStore {
        return JdbcTokenStore(dataSource)
    }

    @Bean
    fun oauthAccessDeniedHandler(): OAuth2AccessDeniedHandler {
        return OAuth2AccessDeniedHandler()
    }

    override fun configure(oauthServer: AuthorizationServerSecurityConfigurer?) {
        oauthServer!!.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()")
            .passwordEncoder(oauthClientPasswordEncoder)
    }

    override fun configure(clients: ClientDetailsServiceConfigurer?) {
        clients!!.jdbc(dataSource)
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer?) {
        endpoints!!.tokenStore(tokenStore()).authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
    }
}