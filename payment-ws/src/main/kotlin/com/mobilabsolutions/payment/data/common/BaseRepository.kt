package com.mobilabsolutions.payment.data.common

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import java.io.Serializable

/**
 * Extends the base JPA repository with functionality provided by the JPA specification executor.
 *
 * @param <T> the entity type
 * @param <ID> the entity id type
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@NoRepositoryBean
interface BaseRepository<T, ID : Serializable> : CriteriaJpaRepository<T, ID>, JpaSpecificationExecutor<T>