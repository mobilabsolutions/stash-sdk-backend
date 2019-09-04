/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.Merchant
import com.mobilabsolutions.payment.data.configuration.BaseRepository
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

    @Modifying
    @Query("UPDATE Merchant m SET m.logo = :logo, m.lastModifiedDate = CURRENT_TIMESTAMP WHERE m.id = :merchantId")
    fun saveLogo(@Param("logo") logo: ByteArray?, @Param("merchantId") merchantId: String)

    @Modifying
    @Query("UPDATE Merchant m SET m.webhookUrl = :webhookUrl, m.webhookUsername = :webhookUsername, m.webhookPassword = :webhookPassword, m.lastModifiedDate = CURRENT_TIMESTAMP WHERE m.id = :merchantId")
    fun updateMerchantWebookCredentials(@Param("merchantId") merchantId: String, @Param("webhookUrl") webhookUrl: String, @Param("webhookUsername") webhookUsername: String, @Param("webhookPassword") webhookPassword: String)

    @Query("SELECT * FROM Merchant m WHERE m.webhook_url <> ''", nativeQuery = true)
    fun getMerchantsByWebhookUrl(): List<Merchant>

    @Query("SELECT m.name FROM merchant m JOIN authority a ON m.id = a.name JOIN merchant_user_authorities mua ON mua.authority_id = a.id " +
        "JOIN merchant_user mu ON mu.id = mua.merchant_user_id WHERE mu.id = :userId", nativeQuery = true)
    fun getMerchantForUser(@Param("userId") userId: String): String?
}
