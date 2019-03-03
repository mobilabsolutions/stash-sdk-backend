package com.mobilabsolutions.payment.data.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AutoGeneratedIdTimeAuditable : Serializable {

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    var createdDate: Instant? = null

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    var lastModifiedDate: Instant? = null
}