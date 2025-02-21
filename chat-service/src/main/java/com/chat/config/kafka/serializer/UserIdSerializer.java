package com.chat.config.kafka.serializer;

import com.chat.domain.entity.user.UserId;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class UserIdSerializer extends JsonSerializer<UserId> {
    @Override
    public void serialize(UserId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.asString());
        }
    }
}
