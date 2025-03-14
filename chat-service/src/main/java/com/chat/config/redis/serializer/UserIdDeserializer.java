package com.chat.config.redis.serializer;

import com.chat.domain.entity.user.UserId;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class UserIdDeserializer extends StdDeserializer<UserId> {

    public UserIdDeserializer() {
        super(UserId.class);
    }

    @Override
    public UserId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return UserId.fromString(value);
    }
}
