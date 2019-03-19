package com.mobilabsolutions.payment.data.domain

import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import org.hibernate.annotations.Type
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
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Entity
@Table(name = "transaction_record")
class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private val id: Long? = null,

    @Column(name = "transaction_id", length = 20)
    var transactionId: String? = null,

    @Column(name = "idempotent_key")
    var idempotentKey: String? = null,

    @Column(name = "currency_id", length = 3)
    var currencyId: String? = null,

    @Column(name = "amount")
    var amount: Int = 0,

    @Column(name = "reason")
    var reason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    var action: TransactionAction? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: TransactionStatus? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    var paymentMethod: PaymentMethod? = null,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "payment_info")
    var paymentInfo: String? = null,

    @Column(name = "merchant_transaction_id")
    var merchantTransactionId: String? = null,

    @Column(name = "merchant_customer_id")
    var merchantCustomerId: String? = null,

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false, foreignKey = ForeignKey(name = "fk_merchant_transactionrecord"))
    var merchant: Merchant? = null,

    @ManyToOne
    @JoinColumn(name = "alias_id", foreignKey = ForeignKey(name = "fk_alias_transactionrecord"))
    var alias: Alias? = null
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

        other as Transaction

        return this.id == other.id
    }
}