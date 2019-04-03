package com.mobilabsolutions.payment.data.domain

import org.hibernate.annotations.Type
import org.springframework.data.util.ProxyUtils
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Entity
@Table(name = "merchant")
class Merchant(
    @Id
    @Column(name = "id")
    var id: String? = null,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "psp_config")
    var pspConfig: String? = null,

    @Column(name = "default_currency_id", length = 3)
    var defaultCurrency: String? = null
) : AutoGeneratedIdTimeAuditable() {

    override fun hashCode(): Int {
        var hash = 7
        hash = 97 * hash + Objects.hashCode(this.id)
        return hash
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as Merchant

        return this.id == other.id
    }

    override fun toString(): String {
        return "Merchant [name=$name, email=$email, defaultCurrency=$defaultCurrency]"
    }
}
