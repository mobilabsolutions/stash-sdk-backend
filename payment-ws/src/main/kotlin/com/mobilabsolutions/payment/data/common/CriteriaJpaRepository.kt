package com.mobilabsolutions.payment.data.common

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.io.Serializable

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@NoRepositoryBean
interface CriteriaJpaRepository<T, ID : Serializable> : JpaRepository<T, ID> {

    /**
     * Executes a query using the given criteria and expects one result.
     *
     * @param <R> the result type
     * @param cls the result type class
     * @param criteria the criteria
     * @return the result
     */
    fun <R> findOne(cls: Class<R>, criteria: Select<T, R>): R?

    /**
     * Executes a query using the given criteria.
     *
     * @param <R> the result type
     * @param cls the result type class
     * @param criteria the criteria
     * @return the result list
    */
    fun <R> findAll(cls: Class<R>, criteria: Select<T, R>): List<R>

    /**
     * Executes a query using the given criteria and sort.
     *
     * @param <R> the result type
     * @param cls the result type class
     * @param criteria the criteria
     * @param sort the result sorting (note : can also be specified directly into the criteria)
     * @return the result list
    */
    fun <R> findAll(cls: Class<R>, criteria: Select<T, R>, sort: Sort): List<R>

    /**
     * Executes a query using the given criteria and pageable.
     *
     * @param <R> the result type
     * @param cls the result type class
     * @param criteria the criteria
     * @param pageable the pageable used to control the amoun and index of returned results
     * @return the result list
    */
    fun <R> findAll(cls: Class<R>, criteria: Select<T, R>, pageable: Pageable): List<R>

    /**
     * Executes the given update specification.
     *
     * @param update the update specification
     * @return the number of rows updated
     */
    fun update(update: Update<T>?): Int

    /**
     * Executes the given delete specification.
     *
     * @param delete the delete specification
     * @return the number of rows deleted
     */
    fun delete(delete: Delete<T>?): Int

    /**
     * Refreshes the state of the entity from the database.
     *
     * @param entity the entity
     * @return the updated entity
     */
    fun refresh(entity: T): T
}