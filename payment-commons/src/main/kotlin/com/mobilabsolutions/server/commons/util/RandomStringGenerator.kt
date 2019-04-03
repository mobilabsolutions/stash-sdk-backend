package com.mobilabsolutions.server.commons.util

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Component
class RandomStringGenerator {
    fun generateRandomAlphanumeric(length: Int): String {
        return RandomStringUtils.randomAlphanumeric(length)
    }
}
