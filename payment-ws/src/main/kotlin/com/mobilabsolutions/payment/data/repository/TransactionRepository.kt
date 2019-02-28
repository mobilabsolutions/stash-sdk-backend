package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.common.BaseRepository
import com.mobilabsolutions.payment.data.domain.Transaction
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface TransactionRepository : BaseRepository<Transaction, Long>