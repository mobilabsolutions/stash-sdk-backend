/*
 * Copyright © MobiLab Solutions GmbH
*/

package com.mobilabsolutions.payment.configuration

import com.mobilabsolutions.payment.data.repository.MerchantRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import java.util.HashMap

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
class CustomTokenConverter(
    private val merchantRepository: MerchantRepository
) : JwtAccessTokenConverter() {

    override fun enhance(token: OAuth2AccessToken?, authentication: OAuth2Authentication?): OAuth2AccessToken {
        val user = authentication?.principal as UserDetails
        val additionalInfo = HashMap<String, Any>()

        val name = merchantRepository.getMerchantForUser(user.username)
        additionalInfo["merchant_name"] = name ?: ""

        (token as DefaultOAuth2AccessToken).additionalInformation = additionalInfo
        val accessToken = super.enhance(token, authentication)
        (accessToken as DefaultOAuth2AccessToken).additionalInformation = HashMap()

        return accessToken
    }
}
