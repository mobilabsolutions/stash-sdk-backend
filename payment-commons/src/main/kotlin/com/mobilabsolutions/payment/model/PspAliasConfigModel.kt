/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@ApiModel(value = "PSP alias config model")
data class PspAliasConfigModel(
    @ApiModelProperty(value = "Payment service provider type", example = "BS_PAYONE")
    val type: String?,

    @ApiModelProperty(value = "Merchant ID", example = "42865")
    val merchantId: String?,

    @ApiModelProperty(value = "Payment service provider mode", example = "test")
    val mode: String?,

    @ApiModelProperty(value = "Portal ID", example = "2030968")
    val portalId: String?,

    @ApiModelProperty(value = "Request name", example = "creditcardcheck")
    val request: String?,

    @ApiModelProperty(value = "API version", example = "3.11")
    val apiVersion: String?,

    @ApiModelProperty(value = "Response enum type", example = "JSON")
    val responseType: String?,

    @ApiModelProperty(value = "Encoding type", example = "UTF-8")
    val encoding: String?,

    @ApiModelProperty(value = "Hash", example = "35996f45100c40d51cffedcddc471f8189fc3568c287871568dc6c8bae1c4d732ded416b502f6191fb6085a2d767ef6f")
    val hash: String?,

    @ApiModelProperty(value = "Account ID", example = "42949")
    val accountId: String?,

    @ApiModelProperty(value = "Public key", example = "bbdjshcjdhdsgf")
    val publicKey: String?,

    @ApiModelProperty(value = "Private key", example = "ncbcjdheufhdhfjh")
    val privateKey: String?,

    @ApiModelProperty(value = "Client token", example = "eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiJk")
    val clientToken: String?,

    @ApiModelProperty(value = "Client encryption key", example = "ABA0B8C5413CD5E0D806DE19CA...")
    val clientEncryptionKey: String?
)
