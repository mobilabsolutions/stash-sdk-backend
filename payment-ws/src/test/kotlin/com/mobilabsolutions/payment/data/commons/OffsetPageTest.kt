package com.mobilabsolutions.payment.data.commons

import com.mobilabsolutions.payment.data.common.OffsetPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
class OffsetPageTest {

    /**
     * Ensure that the page number is always the previous strict page before the offset.
     */
    @Test
    fun test() {
        assertEquals(0, OffsetPage(0, 3).getPageNumber())
        assertEquals(0, OffsetPage(1, 3).getPageNumber())
        assertEquals(0, OffsetPage(2, 3).getPageNumber())
        assertEquals(1, OffsetPage(3, 3).getPageNumber())
    }
}