package org.oasis_eu.portal.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class CustomInstantSerializer extends StdSerializer<Instant> {

    private static DateTimeFormatter formatter =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public CustomInstantSerializer() {
        this(null);
    }

    public CustomInstantSerializer(Class<Instant> t) {
        super(t);
    }

    @Override
    public void serialize
            (Instant value, JsonGenerator gen, SerializerProvider arg2)
            throws IOException {
        gen.writeString(formatter.print(Date.from(value).getTime()));
    }


}