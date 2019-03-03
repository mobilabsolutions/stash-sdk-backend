package com.mobilabsolutions.payment.data.domain

import com.mobilabsolutions.payment.data.enum.KeyType
import org.springframework.data.util.ProxyUtils
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Entity
@Table(name = "merchant_api_key")
class MerchantApiKey(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private val id: Long? = null,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "key")
    var key: String? = null,

    @Column(name = "active")
    var active: Boolean = true,

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type")
    var keyType: KeyType? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "merchant_id", nullable = false, foreignKey = ForeignKey(name = "fk_merchant_merchantapikey"))
    var merchant: Merchant = Merchant()
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

        other as MerchantApiKey

        return this.id == other.id
    }

    override fun toString(): String {
        return "MerchantApiKey [name=$name, key=$key, active=$active, keyType=$keyType, merchant=$merchant]"
    }
}