package com.mobilabsolutions.server.commons.util;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
public class DefaultInstantDeserializer extends InstantDeserializer<Instant> {

    public DefaultInstantDeserializer() {
        super(Instant.class, DateTimeFormatter.ISO_INSTANT,
            Instant::from,
            a -> Instant.ofEpochMilli(a.value),
            a -> Instant.ofEpochSecond(a.integer, a.fraction),
            null,
            true);
    }
}
