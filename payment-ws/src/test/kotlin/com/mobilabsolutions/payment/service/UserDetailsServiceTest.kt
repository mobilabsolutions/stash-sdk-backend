package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.MerchantUser
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
import com.mobilabsolutions.payment.data.repository.MerchantUserRepository
import com.mobilabsolutions.payment.model.MerchantUserChangePasswordModel
import com.mobilabsolutions.payment.model.MerchantUserCreateModel
import com.mobilabsolutions.payment.model.MerchantUserUpdateModel
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
import org.mockito.Mockito.`when`
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

        `when`(merchantUserRepository.findByEmail(knownEmail)).thenReturn(
            MerchantUser(
                email = knownEmail,
                password = userPassword,
                authorities = setOf()
            )
        )
        `when`(merchantUserRepository.findByEmail(unknownEmail)).thenReturn(null)
        doNothing().`when`(merchantUserRepository).updateMerchantUser(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString()
        )
        `when`(userPasswordEncoder.matches(userPassword, userPassword)).thenReturn(true)
        `when`(userPasswordEncoder.encode(userPassword)).thenReturn(anotherUserPassword)
        `when`(authorityRepository.getAuthorityByName(knownMerchant)).thenReturn(Mockito.mock(Authority::class.java))
        `when`(authorityRepository.getAuthorityByName(unKnownMerchant)).thenReturn(null)
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
            Mockito.mock(MerchantUserUpdateModel::class.java)
        )
    }

    @Test
    fun `update successfully with admin merchant user`() {
        userDetailsService.updateMerchantUser("some email", "admin", Mockito.mock(MerchantUserUpdateModel::class.java))
    }

    @Test
    fun `update merchant user without related rights`() {
        Assertions.assertThrows(ApiException::class.java) {
            userDetailsService.updateMerchantUser(
                "some email",
                "other email",
                Mockito.mock(MerchantUserUpdateModel::class.java)
            )
        }
    }

    @Test
    fun `change merchant user password successfully`() {
        userDetailsService.changePasswordMerchantUser(
            knownEmail,
            knownEmail,
            MerchantUserChangePasswordModel(userPassword, "new password")
        )
    }

    @Test
    fun `change merchant user password unsuccessfully`() {
        Assertions.assertThrows(ApiException::class.java) {
            userDetailsService.changePasswordMerchantUser(
                knownEmail,
                knownEmail,
                MerchantUserChangePasswordModel(anotherUserPassword, "new password")
            )
        }
    }

    @Test
    fun `change merchant user password with admin rights`() {
        userDetailsService.changePasswordMerchantUser(
            knownEmail,
            "admin",
            MerchantUserChangePasswordModel(userPassword, "new password")
        )
    }

    @Test
    fun `create merchant user successfully`() {
        userDetailsService.createMerchantUser(
            knownMerchant,
            MerchantUserCreateModel(knownEmail, userPassword, "test name", "test lastname", "test locale")
        )
    }

    @Test
    fun `create merchant user unsuccessfully`() {
        Assertions.assertThrows(ApiException::class.java) {
            userDetailsService.createMerchantUser(
                unKnownMerchant,
                Mockito.mock(MerchantUserCreateModel::class.java)
            )
        }
    }
}