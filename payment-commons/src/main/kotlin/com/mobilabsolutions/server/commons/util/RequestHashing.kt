package com.mobilabsolutions.server.commons.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.hash.Hashing
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Component
class RequestHashing(private val objectMapper: ObjectMapper) {
    fun hashRequest(request: Any): String {
        return Hashing.md5().hashString(objectMapper.writeValueAsString(request), Charsets.UTF_8).toString()
    }
}
