/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data.configuration

import com.mobilabsolutions.payment.data.Authority
import com.mobilabsolutions.payment.data.MerchantUser
import com.mobilabsolutions.payment.data.OAuthClientDetails
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.data.repository.OAuthClientDetailsRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
@ConfigurationProperties(prefix = "initial.data.loader")
class InitialDataLoader(
    private val merchantUserRepository: MerchantUserRepository,
    private val oAuthClientDetailsRepository: OAuthClientDetailsRepository,
    @Qualifier("userPasswordEncoder") private val userPasswordEncoder: PasswordEncoder,
    @Qualifier("clientPasswordEncoder") private val oauthClientPasswordEncoder: PasswordEncoder
) : ApplicationListener<ContextRefreshedEvent> {

    lateinit var oauthClientId: String
    lateinit var oauthClientPassword: String
    lateinit var adminUsername: String
    lateinit var adminPassword: String

    private var alreadySetup = false

    @Transactional
    override fun onApplicationEvent(event: ContextRefreshedEvent) {

        if (alreadySetup)
            return

        logger.info { "Loading initial data..." }

        val oAuthClientDetails = OAuthClientDetails(
            clientId = oauthClientId,
            resourceIds = "payment-sdk-rest-api",
            clientSecret = oauthClientPasswordEncoder.encode(oauthClientPassword),
            scope = "user",
            authorizedGrantTypes = "password,refresh_token",
            accessTokenValidity = 300,
            refreshTokenValidity = 3600
        )
        oAuthClientDetailsRepository.save(oAuthClientDetails)

        val merchantUser = MerchantUser(
            email = adminUsername,
            password = userPasswordEncoder.encode(adminPassword),
            authorities = setOf(Authority(id = 1, name = "admin"))
        )
        merchantUserRepository.save(merchantUser)

        alreadySetup = true
    }

    companion object : KLogging()
}
