/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.service

import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.service.Psp
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class PspRegistry(private val psp: List<Psp>) {
    private var pspMap: Map<PaymentServiceProvider, Psp> = mapOf()

    fun find(provider: PaymentServiceProvider): Psp? {
        return pspMap[provider]
    }

    @PostConstruct
    fun init() {
        pspMap = psp.map { it.getProvider() to it }.toMap()
    }
}
