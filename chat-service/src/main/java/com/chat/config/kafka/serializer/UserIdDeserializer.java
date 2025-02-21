package com.chat.config.kafka.serializer;

import com.chat.domain.entity.user.UserId;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class UserIdDeserializer extends JsonDeserializer<UserId> {
    @Override
    public UserId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        return value == null ? null : UserId.fromString(value);
    }
}
