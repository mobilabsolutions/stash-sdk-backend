package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.MerchantApiKey
import com.mobilabsolutions.payment.data.enum.KeyType
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface MerchantApiKeyRepository : BaseRepository<MerchantApiKey, Long> {
    fun getFirstByActiveAndKeyTypeAndKey(active: Boolean, keyType: KeyType, key: String): MerchantApiKey?
}