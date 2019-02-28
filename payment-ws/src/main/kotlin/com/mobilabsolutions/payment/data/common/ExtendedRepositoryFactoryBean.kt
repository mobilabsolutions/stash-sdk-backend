package com.mobilabsolutions.payment.data.common

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.core.RepositoryInformation
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import java.io.Serializable
import javax.persistence.EntityManager

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
class ExtendedRepositoryFactoryBean<R : JpaRepository<T, I>, T, I : Serializable>(repositoryInterface: Class<out R>)
    : JpaRepositoryFactoryBean<R, T, I>(repositoryInterface) {

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {

        return ExtendedRepositoryFactory<T, I>(entityManager)
    }

    private class ExtendedRepositoryFactory<T, I : Serializable>
        internal constructor(private val entityManager: EntityManager) : JpaRepositoryFactory(entityManager) {

        override fun getTargetRepository(information: RepositoryInformation, entityManager: EntityManager): ExtendedJpaRepository<T, I> {
            return ExtendedJpaRepository(information.domainType as Class<T>, this.entityManager)
        }

        override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> {
            return ExtendedJpaRepository::class.java
        }
    }
}