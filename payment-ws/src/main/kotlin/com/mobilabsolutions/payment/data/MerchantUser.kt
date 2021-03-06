/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data

import com.mobilabsolutions.payment.data.configuration.AutoGeneratedIdTimeAuditable
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Entity
@Table(name = "merchant_user")
class MerchantUser(

    @Id
    @Column(name = "id")
    var email: String? = null,

    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "locale")
    var locale: String? = null,

    @Column(name = "password")
    var password: String? = null,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "merchant_user_authorities",
        joinColumns = [JoinColumn(name = "merchant_user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "authority_id", referencedColumnName = "id")]
    )
    var authorities: Set<Authority>? = null
) : AutoGeneratedIdTimeAuditable() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MerchantUser

        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }
}
