package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.service.MerchantService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@RestController
@RequestMapping(AliasController.BASE_URL)
class MerchantController(
    private val merchantService: MerchantService
) {

    @RequestMapping(
        MERCHANT_URL, method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    fun getMerchant(id: String) = merchantService.getMerchant(id)

    companion object {
        const val BASE_URL = "merchant"
        const val MERCHANT_URL = "/{Merchant-Id}"
    }
}