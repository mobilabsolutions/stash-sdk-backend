package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.common.BaseRepository
import com.mobilabsolutions.payment.data.domain.MerchantUser
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface MerchantUserRepository : BaseRepository<MerchantUser, Long>