package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.MerchantUser
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.server.commons.exception.ApiError
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDetailsServiceImpl(private val merchantUserRepository: MerchantUserRepository) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        return merchantUserRepository.findByEmail(email)?.toUserDetails() ?: throw ApiError.builder().withMessage("Bad credentials").build().asUnauthorized()
    }

    private fun MerchantUser.toUserDetails() = User(email, password, enabled, true, true, true, authorities)
}