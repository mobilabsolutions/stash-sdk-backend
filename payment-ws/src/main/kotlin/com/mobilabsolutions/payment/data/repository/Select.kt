package com.mobilabsolutions.payment.data.repository

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * Creates a {@code WHERE} clause and sets the selection for a criteria select.
 *
 * @param <T> the root domain type
 * @param <R> the query result type
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
interface Select<T, R> {

    /**
     * Sets the selection for the given query and creates the predicate which will be used for the `WHERE` clause.
     *
     * @param cb the criteria builder
     * @param root the query root
     * @param select the criteria select
     * @return the query predicate
     */
    fun toPredicate(cb: CriteriaBuilder, root: Root<T>, select: CriteriaQuery<R>): Predicate

}