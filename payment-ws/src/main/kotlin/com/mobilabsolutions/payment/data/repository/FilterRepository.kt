/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.Filter
import com.mobilabsolutions.payment.data.configuration.BaseRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Repository
interface FilterRepository : BaseRepository<Filter, String> {
    fun getFilterById(id: String): Filter?

    fun getFiltersByMerchantId(merchantId: String): List<Filter>

    @Modifying
    fun deleteFilterById(id: String): Int
}
