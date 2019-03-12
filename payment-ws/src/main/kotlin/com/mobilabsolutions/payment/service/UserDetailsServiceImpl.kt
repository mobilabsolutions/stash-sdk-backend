package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.MerchantUser
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDetailsServiceImpl(private val merchantUserRepository: MerchantUserRepository) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(userName: String?): UserDetails {
        return merchantUserRepository.findByUsername(userName)?.toUserDetails() ?: throw UsernameNotFoundException(
            userName
        )
    }

    private fun MerchantUser.toUserDetails() = User(username, password, enabled, true, true, true, authorities)
}