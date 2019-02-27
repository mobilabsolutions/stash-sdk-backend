package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Alias
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface AliasRepository : BaseRepository<Alias, String> {
}