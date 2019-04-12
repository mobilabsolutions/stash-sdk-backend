package com.mobilabsolutions.payment.bspayone.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ApiModel(value = "BS Payone Delete Alias")
data class BsPayoneDeleteAliasRequestModel(
    @ApiModelProperty(value = "BS Payone customer id", example = "12378")
    @JsonProperty(value = "customerid")
    private val customerId: String?,

    @ApiModelProperty(value = "Whether the card data should be deleted", example = "yes")
    @JsonProperty(value = "delete_carddata")
    private val deleteCardData: String?,

    @ApiModelProperty(value = "Whether the bank account data should be deleted", example = "no")
    @JsonProperty(value = "delete_bankaccountdata")
    private val deleteBankAccountData: String?
)
