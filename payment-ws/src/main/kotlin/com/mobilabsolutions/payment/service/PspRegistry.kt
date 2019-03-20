package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import org.springframework.stereotype.Component
import java.util.HashMap
import javax.annotation.PostConstruct

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class PspRegistry(private val psp: List<Psp>) {
    private var pspMap: Map<PaymentServiceProvider, Psp> = HashMap()

    fun find(provider: PaymentServiceProvider): Psp? {
        return pspMap[provider]
    }

    @PostConstruct
    fun init() {
        pspMap = psp.map { it.getProvider() to it }.toMap()
    }
}