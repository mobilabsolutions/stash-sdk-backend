/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.data

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Entity
@Table(name = "oauth_client_details")
class OAuthClientDetails(
    @Id
    @Column(name = "client_id")
    var clientId: String,

    @Column(name = "resource_ids")
    var resourceIds: String,

    @Column(name = "client_secret")
    var clientSecret: String,

    @Column(name = "scope")
    var scope: String,

    @Column(name = "authorized_grant_types")
    var authorizedGrantTypes: String,

    @Column(name = "web_server_redirect_uri")
    var webServerRedirectUri: String? = null,

    @Column(name = "authorities")
    var authorities: String? = null,

    @Column(name = "access_token_validity", columnDefinition = "int4")
    var accessTokenValidity: Long,

    @Column(name = "refresh_token_validity", columnDefinition = "int4")
    var refreshTokenValidity: Long,

    @Column(name = "additional_information", columnDefinition = "varchar(4096)")
    var additionalInformation: String? = null,

    @Column(name = "autoapprove")
    var autoapprove: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OAuthClientDetails

        if (clientId != other.clientId) return false

        return true
    }

    override fun hashCode(): Int {
        return clientId.hashCode()
    }
}
