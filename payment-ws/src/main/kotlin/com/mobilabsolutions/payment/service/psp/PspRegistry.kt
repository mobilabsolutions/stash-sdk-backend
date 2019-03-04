package com.mobilabsolutions.payment.service.psp

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import org.springframework.stereotype.Component
import java.util.HashMap
import javax.annotation.PostConstruct

@Component
class PspRegistry(private val psp: List<Psp>) {

    private var pspMap: Map<PaymentServiceProvider, Psp> = HashMap<PaymentServiceProvider, Psp>()

    fun find(provider: PaymentServiceProvider): Psp? {
        return pspMap[provider]
    }

    @PostConstruct
    fun init() {
        pspMap = psp.map { it.getProvider() to it }.toMap()
    }
}