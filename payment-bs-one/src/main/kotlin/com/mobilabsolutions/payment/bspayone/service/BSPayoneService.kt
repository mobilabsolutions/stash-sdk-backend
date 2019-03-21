package com.mobilabsolutions.payment.bspayone.service

import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import org.springframework.stereotype.Service

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Service
class BSPayoneService(private val bsPayoneProperties: BsPayoneProperties)