package com.mobilabsolutions.payment.data.domain

import org.springframework.data.domain.Persistable
import org.springframework.data.util.ProxyUtils
import java.beans.Transient
import java.io.Serializable
import java.util.*
import javax.persistence.*

@MappedSuperclass
abstract class BasePersistable<PK : Serializable> : Persistable<PK> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var id: PK? = null

    override fun getId(): PK? {
        return id
    }

    /**
     * Must be [Transient] in order to ensure that no JPA provider complains because of a missing setter.
     *
     * @see org.springframework.data.domain.Persistable.isNew
     */
    @Transient
    override fun isNew() = null == id

    override fun toString(): String {
        return String.format(TO_STRING_FORMAT, this.javaClass, this.id, this.isNew)
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 97 * hash + Objects.hashCode(this.isNew)
        hash = 97 * hash + Objects.hashCode(this.id)
        return hash
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as BasePersistable<*>

        return if (null == this.getId()) false else this.id == other.id
    }

    companion object {
        private const val TO_STRING_FORMAT = "%s{id=%s, new=%s}"
    }

}