package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.MerchantUser
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface MerchantUserRepository : BaseRepository<MerchantUser, String> {

    @Query("select distinct user from MerchantUser user inner join fetch user.authorities as authorities where user.email = :email")
    fun findByEmail(@Param("email") email: String?): MerchantUser?
}