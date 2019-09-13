/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.PasswordResetToken
import com.mobilabsolutions.payment.data.configuration.BaseRepository
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Repository
interface PasswordResetTokenRepository : BaseRepository<PasswordResetToken, String> {
    fun getByToken(token: String): PasswordResetToken?
}
