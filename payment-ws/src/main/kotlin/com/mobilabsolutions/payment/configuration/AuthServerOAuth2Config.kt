/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.configuration

import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.service.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore
import javax.sql.DataSource

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Configuration
@EnableAuthorizationServer
@ConfigurationProperties(prefix = "authorization.server")
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

    @Autowired
    private lateinit var exceptionTranslator: CustomWebResponseExceptionTranslator

    @Autowired
    private lateinit var merchantRepository: MerchantRepository

    lateinit var signingKey: String

    @Bean
    fun tokenStore(): TokenStore {
        return JwtTokenStore(jwtTokenEnhancer())
    }

    @Bean
    fun oauthAccessDeniedHandler(): OAuth2AccessDeniedHandler {
        return OAuth2AccessDeniedHandler()
    }

    @Bean
    fun jwtTokenEnhancer(): JwtAccessTokenConverter {
        val converter = CustomTokenConverter(merchantRepository)
        converter.setSigningKey(signingKey)
        return converter
    }

    override fun configure(oauthServer: AuthorizationServerSecurityConfigurer?) {
        oauthServer!!.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()")
            .passwordEncoder(oauthClientPasswordEncoder)
    }

    override fun configure(clients: ClientDetailsServiceConfigurer?) {
        clients!!.jdbc(dataSource)
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer?) {
        endpoints!!.tokenStore(tokenStore()).tokenEnhancer(jwtTokenEnhancer())
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
            .exceptionTranslator(exceptionTranslator)
    }
}
