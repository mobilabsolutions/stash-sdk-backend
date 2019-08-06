/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.bspayone.data.enum

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
enum class BsPayoneClearingType(val type: String) {
    CC("cc"),
    SEPA("elv")
}
