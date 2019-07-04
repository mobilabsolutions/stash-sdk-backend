/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.MerchantUser
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.model.request.MerchantUserPasswordRequestModel
import com.mobilabsolutions.payment.model.request.MerchantUserRequestModel
import com.mobilabsolutions.payment.model.request.EditMerchantUserRequestModel
import com.mobilabsolutions.server.commons.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDetailsServiceTest {

    @InjectMocks
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Mock
    private lateinit var merchantUserRepository: MerchantUserRepository

    @Mock
    private lateinit var authorityRepository: AuthorityRepository

    @Mock
    private lateinit var userPasswordEncoder: PasswordEncoder

    private val knownMerchant = "known merchant"
    private val unKnownMerchant = "unknown merchant"
    private val knownEmail = "known email"
    private val unknownEmail = "unknown email"
    private val userPassword = "some password"
    private val anotherUserPassword = "another password"

    @BeforeAll
    fun beforeAll() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(userDetailsService, "adminUsername", "admin")

        Mockito.`when`(merchantUserRepository.findByEmail(knownEmail)).thenReturn(
            MerchantUser(
                email = knownEmail,
                password = userPassword,
                authorities = setOf()
            )
        )
        Mockito.`when`(merchantUserRepository.findByEmail(unknownEmail)).thenReturn(null)
        doNothing().`when`(merchantUserRepository).updateMerchantUser(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString()
        )
        Mockito.`when`(userPasswordEncoder.matches(userPassword, userPassword)).thenReturn(true)
        Mockito.`when`(userPasswordEncoder.encode(userPassword)).thenReturn(anotherUserPassword)
        Mockito.`when`(authorityRepository.getAuthorityByName(knownMerchant)).thenReturn(Mockito.mock(Authority::class.java))
        Mockito.`when`(authorityRepository.getAuthorityByName(unKnownMerchant)).thenReturn(null)
    }

    @Test
    fun `find merchant user successfully`() {
        userDetailsService.loadUserByUsername(knownEmail)
    }

    @Test
    fun `find unknown merchant user`() {
        Assertions.assertThrows(ApiException::class.java) { userDetailsService.loadUserByUsername(unknownEmail) }
    }

    @Test
    fun `update successfully with no admin merchant user`() {
        userDetailsService.updateMerchantUser(
            "some email",
            "some email",
            Mockito.mock(EditMerchantUserRequestModel::class.java)
        )
    }

    @Test
    fun `update successfully with admin merchant user`() {
        userDetailsService.updateMerchantUser("some email", "admin", Mockito.mock(EditMerchantUserRequestModel::class.java))
    }

    @Test
    fun `update merchant user without related rights`() {
        Assertions.assertThrows(ApiException::class.java) {
            userDetailsService.updateMerchantUser(
                "some email",
                "other email",
                Mockito.mock(EditMerchantUserRequestModel::class.java)
            )
        }
    }

    @Test
    fun `change merchant user password successfully`() {
        userDetailsService.changePasswordMerchantUser(
            knownEmail,
            knownEmail,
            MerchantUserPasswordRequestModel(userPassword, "new password")
        )
    }

    @Test
    fun `change merchant user password unsuccessfully`() {
        Assertions.assertThrows(ApiException::class.java) {
            userDetailsService.changePasswordMerchantUser(
                knownEmail,
                knownEmail,
                MerchantUserPasswordRequestModel(anotherUserPassword, "new password")
            )
        }
    }

    @Test
    fun `change merchant user password with admin rights`() {
        userDetailsService.changePasswordMerchantUser(
            knownEmail,
            "admin",
            MerchantUserPasswordRequestModel(userPassword, "new password")
        )
    }

    @Test
    fun `create merchant user successfully`() {
        userDetailsService.createMerchantUser(
            knownMerchant,
            MerchantUserRequestModel(unknownEmail, userPassword, "test name", "test lastname", "test locale")
        )
    }

    @Test
    fun `create merchant user unsuccessfully`() {
        Assertions.assertThrows(ApiException::class.java) {
            userDetailsService.createMerchantUser(
                unKnownMerchant,
                Mockito.mock(MerchantUserRequestModel::class.java)
            )
        }
    }

    @Test
    fun `create already existing merchant user`() {
        Assertions.assertThrows(ApiException::class.java) {
            userDetailsService.createMerchantUser(
                knownMerchant,
                MerchantUserRequestModel(knownEmail, userPassword, "test name", "test lastname", "test locale")
            )
        }
    }
}
