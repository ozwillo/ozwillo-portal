package org.oasis_eu.portal.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.time.Instant;

public class CustomInstantDeserializer extends StdDeserializer<Instant> {

    private static DateTimeFormatter formatter =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public CustomInstantDeserializer() {
        this(null);
    }

    public CustomInstantDeserializer(Class<Instant> t) {
        super(t);
    }

    @Override
    public Instant deserialize (JsonParser jp, DeserializationContext ctxt) throws IOException {
        String str = jp.readValueAs(String.class);
        return formatter.parseLocalDateTime(str).toDate().toInstant();
    }


}