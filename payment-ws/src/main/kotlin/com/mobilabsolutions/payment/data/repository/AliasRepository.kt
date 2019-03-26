package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.Alias
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Repository
interface AliasRepository : BaseRepository<Alias, String> {
    fun getFirstById(id: String): Alias?

    @Modifying
    @Query("UPDATE Alias a SET a.pspAlias = :pspAlias, a.extra = :extra, a.lastModifiedDate = CURRENT_TIMESTAMP WHERE a.id = :aliasId")
    fun updateAlias(@Param("pspAlias") pspAlias: String?, @Param("extra") extra: String?, @Param("aliasId") aliasId: String)

    fun deleteAliasById(aliasId: String): Int
}