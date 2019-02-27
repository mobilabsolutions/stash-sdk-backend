package com.mobilabsolutions.payment.data.domain

import org.springframework.data.util.ProxyUtils
import java.util.*
import javax.persistence.*

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Entity
@Table(name = "merchant_user")
class MerchantUser : AutoGeneratedIdTimeAuditable() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private val id: Long? = null

    @Column(name = "username")
    var username: String? = null

    @Column(name = "password")
    var password: String? = null

    @Column(name = "locale")
    var locale: String? = null

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false, foreignKey = ForeignKey(name = "fk_merchant_merchantuser"))
    var merchant: Merchant = Merchant()

    override fun hashCode(): Int {
        var hash = 7
        hash = 97 * hash + Objects.hashCode(this.id)
        return hash
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as MerchantUser

        return this.id == other.id
    }

    override fun toString(): String {
        return "MerchantUser [username=$username, locale=$locale, merchant=$merchant]"
    }
}