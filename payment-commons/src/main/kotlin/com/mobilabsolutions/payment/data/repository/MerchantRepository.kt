package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Merchant
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface MerchantRepository : BaseRepository<Merchant, String> {
    fun getMerchantById(id: String): Merchant?

    @Modifying
    @Query("UPDATE Merchant m SET m.pspConfig = :pspConfig, m.lastModifiedDate = CURRENT_TIMESTAMP WHERE m.id = :merchantId")
    fun updateMerchant(@Param("pspConfig") pspConfig: String?, @Param("merchantId") merchantId: String)
}