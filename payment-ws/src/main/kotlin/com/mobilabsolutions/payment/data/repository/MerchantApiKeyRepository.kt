package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface MerchantApiKeyRepository : BaseRepository<MerchantApiKey, Long> {
    fun getFirstByActiveAndKeyTypeAndKey(active: Boolean, keyType: KeyType, key: String): MerchantApiKey?

    fun getAllByMerchantId(merchantId: String): ArrayList<MerchantApiKey>

    fun getFirstById(apiKeyId: Long): MerchantApiKey?

    @Modifying
    @Query("UPDATE MerchantApiKey m SET m.name = :name WHERE m.id = :id")
    fun editApiKey(@Param("name") name: String?, @Param("id") id: Long): Int

    @Modifying
    fun deleteMerchantApiKeyById(apiKeyId: Long): Int
}