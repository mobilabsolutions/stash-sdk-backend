package com.mobilabsolutions.payment.data.common

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaDelete
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

/**
 * Creates a {@code WHERE} clause and sets the selection for a criteria delete.
 *
 * @param <T> the root domain type
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
interface Delete<T> {

    /**
     * Sets the selection for the given query and creates the predicate which will be used for the `WHERE` clause.
     *
     * @param cb the criteria builder
     * @param root the query root
     * @param delete the criteria delete
     * @return the query predicate
     */
    fun toPredicate(cb: CriteriaBuilder, root: Root<T>, delete: CriteriaDelete<*>): Predicate
}