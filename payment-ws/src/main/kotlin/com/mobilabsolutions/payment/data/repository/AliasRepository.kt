package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface AliasRepository : BaseRepository<Alias, String> {
    fun getFirstByIdAndActive(id: String, active: Boolean): Alias?

    @Query("SELECT DISTINCT a FROM Alias a WHERE a.idempotentKey = :idempotentKey AND a.active = :active AND a.merchant = :merchant AND a.psp = :psp")
    fun getByIdempotentKeyAndActiveAndMerchantAndPspType(@Param("idempotentKey") idempotentKey: String, @Param("active") active: Boolean, @Param("merchant") merchant: Merchant, @Param("psp") psp: PaymentServiceProvider): Alias?

    @Modifying
    @Query("UPDATE Alias a SET a.pspAlias = :pspAlias, a.extra = :extra, a.userAgent = :userAgent, a.lastModifiedDate = CURRENT_TIMESTAMP WHERE a.id = :aliasId")
    fun updateAlias(@Param("pspAlias") pspAlias: String?, @Param("extra") extra: String?, @Param("aliasId") aliasId: String, @Param("userAgent") userAgent: String?)
}
