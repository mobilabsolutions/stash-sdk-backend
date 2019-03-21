package com.mobilabsolutions.payment.data.domain

import org.springframework.data.util.ProxyUtils
import org.springframework.security.core.GrantedAuthority
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Entity
@Table(name = "authority", uniqueConstraints = [UniqueConstraint(columnNames = ["name"])])
class Authority(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private val id: Long? = null,

    @Column(name = "name")
    private val name: String? = null

) : GrantedAuthority {
    override fun getAuthority() = name

    override fun hashCode(): Int {
        var hash = 7
        hash = 97 * hash + Objects.hashCode(this.id)
        return hash
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as Authority

        return this.id == other.id
    }
}