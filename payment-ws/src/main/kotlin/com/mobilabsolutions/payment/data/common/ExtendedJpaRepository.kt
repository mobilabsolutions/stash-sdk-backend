package com.mobilabsolutions.payment.data.common

import com.mobilabsolutions.payment.data.common.Select
import com.mobilabsolutions.payment.data.common.Update
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.query.QueryUtils
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import java.io.Serializable
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.TypedQuery

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
class ExtendedJpaRepository<T, ID : Serializable>(domainClass: Class<T>, var em: EntityManager) :
        SimpleJpaRepository<T, ID>(domainClass, em), CriteriaJpaRepository<T, ID> {

    /**
     * Creates the criteria query from the given criteria.
     *
     * @param <R> the result type
     * @param cls the result type class
     * @param criteria the criteria
     * @param sort the sort order
     * @return the typed query
    </R> */
    protected fun <R> query(cls: Class<R>, criteria: Select<T, R>?, sort: Sort?): TypedQuery<R> {

        val cb = this.em.criteriaBuilder
        val criteriaQuery = cb.createQuery(cls)
        val root = criteriaQuery.from(this.domainClass)

        if (criteria != null) {
            val predicate = criteria.toPredicate(cb, root, criteriaQuery)

            if (predicate != null) {
                criteriaQuery.where(predicate)
            }

        }

        if (sort != null) {
            criteriaQuery.orderBy(QueryUtils.toOrders(sort, root, cb))
        }

        return this.em.createQuery(criteriaQuery)
    }

    override fun <R> findOne(cls: Class<R>, criteria: Select<T, R>): R? {
        try {
            return this.query(cls, criteria, null).singleResult
        } catch (e: NoResultException) {
            return null
        }

    }

    override fun <R> findAll(cls: Class<R>, criteria: Select<T, R>): List<R> {
        return this.query(cls, criteria, null).resultList
    }

    override fun <R> findAll(cls: Class<R>, criteria: Select<T, R>, sort: Sort): List<R> {
        return this.query(cls, criteria, sort).resultList
    }

    override fun <R> findAll(cls: Class<R>, criteria: Select<T, R>, pageable: Pageable): List<R> {
        val query = this.query(cls, criteria, pageable.sort)
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize
        return query.resultList
    }

    override fun update(update: Update<T>?): Int {
        val cb = this.em.criteriaBuilder
        val criteriaUpdate = cb.createCriteriaUpdate(this.domainClass)
        val root = criteriaUpdate.from(this.domainClass)

        if (update != null) {
            val predicate = update.toPredicate(cb, root, criteriaUpdate)

            if (predicate != null) {
                criteriaUpdate.where(predicate)
            }

        }

        return this.em.createQuery(criteriaUpdate).executeUpdate()
    }

    override fun delete(delete: Delete<T>?): Int {
        val cb = this.em.criteriaBuilder
        val criteriaDelete = cb.createCriteriaDelete(this.domainClass)
        val root = criteriaDelete.from(this.domainClass)

        if (delete != null) {
            val predicate = delete.toPredicate(cb, root, criteriaDelete)

            if (predicate != null) {
                criteriaDelete.where(predicate)
            }

        }

        return this.em.createQuery(criteriaDelete).executeUpdate()
    }

    override fun refresh(entity: T): T {
        em.refresh(entity)
        return entity
    }
}