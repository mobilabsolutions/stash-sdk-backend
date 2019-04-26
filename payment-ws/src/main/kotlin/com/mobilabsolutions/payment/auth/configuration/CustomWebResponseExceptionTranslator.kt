package com.mobilabsolutions.payment.auth.configuration

import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class CustomWebResponseExceptionTranslator : WebResponseExceptionTranslator {
    override fun translate(e: Exception?): ResponseEntity<OAuth2Exception> {
        return with(ResponseEntity(OAuth2Exception.create(if (e is ClientAuthenticationException) e.oAuth2ErrorCode else null, e?.message), HttpStatus.UNAUTHORIZED)) {
            body?.addAdditionalInformation("error_code", ApiErrorCode.AUTHENTICATION_ERROR.code)
            this
        }
    }
}
