/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data

import com.mobilabsolutions.payment.data.configuration.AutoGeneratedIdTimeAuditable
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.ForeignKey
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Entity
@Table(name = "alias")
class Alias(
    @Id
    @Column(name = "id", length = 20)
    var id: String? = null,

    @Column(name = "idempotent_key")
    var idempotentKey: String? = null,

    @Column(name = "psp_alias")
    var pspAlias: String? = null,

    @Column(name = "active")
    var active: Boolean = true,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "extra")
    var extra: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "psp")
    var psp: PaymentServiceProvider? = null,

    @Column(name = "user_agent")
    var userAgent: String? = null,

    @Column(name = "request_hash")
    var requestHash: String? = null,

    @ManyToOne
    @JoinColumn(name = "merchant_id", foreignKey = ForeignKey(name = "fk_merchant_alias"))
    var merchant: Merchant? = Merchant()
) : AutoGeneratedIdTimeAuditable() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Alias

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}