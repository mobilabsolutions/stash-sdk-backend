package com.mobilabsolutions.payment.model.request

import com.mobilabsolutions.payment.validation.CountryCode
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "PSP Upsert Configuration Request")
data class PspUpsertConfigRequestModel(
    @ApiModelProperty(value = "Merchant ID", example = "42865")
    val merchantId: String?,

    @ApiModelProperty(value = "Portal ID", example = "2030968")
    val portalId: String?,

    @ApiModelProperty(value = "Key", example = "1234567890")
    val key: String?,

    @ApiModelProperty(value = "Account ID", example = "42949")
    val accountId: String?,

    @ApiModelProperty(value = "Sandbox merchant ID", example = "42865")
    val sandboxMerchantId: String?,

    @ApiModelProperty(value = "Sandbox (test) public key", example = "bbdjshcjdhdsgf")
    val sandboxPublicKey: String?,

    @ApiModelProperty(value = "Sandbox (test) private key", example = "ncbcjdheufhdhfjh")
    val sandboxPrivateKey: String?,

    @ApiModelProperty(value = "Production public key", example = "bbdjshcjdhdsgf")
    val publicKey: String?,

    @ApiModelProperty(value = "Production private key", example = "ncbcjdheufhdhfjh")
    val privateKey: String?,

    @ApiModelProperty(value = "Default flag", example = "true")
    val default: Boolean = true,

    @ApiModelProperty(value = "Currency", example = "EUR")
    val currency: String?,

    @ApiModelProperty(value = "Country code of account holder", example = "DE")
    @field:CountryCode
    val country: String?,

    @ApiModelProperty(value = "Locale of the merchant user", example = "de-DE")
    val locale: String?
)
