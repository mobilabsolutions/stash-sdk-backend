package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.AliasRequestModel
import com.mobilabsolutions.payment.model.AliasResponseModel
import com.mobilabsolutions.payment.service.AliasService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@RestController
@RequestMapping(AliasController.BASE_URL)
class AliasController(private val aliasService: AliasService) {

    @RequestMapping(method = [RequestMethod.POST],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createAlias(@RequestHeader(value = "Public-Key") publicKey: String, @RequestHeader(value = "PSP-Type") pspType: String): ResponseEntity<AliasResponseModel> = ResponseEntity.status(HttpStatus.CREATED).body(aliasService.createAlias(publicKey, pspType))

    @RequestMapping(EXCHANGE_ALIAS_URL, method = [RequestMethod.PUT],
            consumes = [MediaType.APPLICATION_JSON_VALUE],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    fun exchangeAlias(
        @RequestHeader(value = "Public-Key") publicKey: String,
        @PathVariable("aliasId") aliasId: String,
        @Valid @RequestBody alias: AliasRequestModel
    ): ResponseEntity<AliasResponseModel> = ResponseEntity.status(HttpStatus.OK).body(aliasService.exchangeAlias(publicKey, aliasId, alias))

    companion object {
        const val BASE_URL = "/api/v1/alias"
        const val EXCHANGE_ALIAS_URL = "/{aliasId}"
    }
}