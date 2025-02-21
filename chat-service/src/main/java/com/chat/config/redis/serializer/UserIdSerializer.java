package com.chat.config.redis.serializer;

import com.chat.domain.entity.user.UserId;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class UserIdSerializer extends StdSerializer<UserId> {

    public UserIdSerializer() {
        super(UserId.class);
    }

    @Override
    public void serialize(UserId userId, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (userId != null && userId.value() != null) {
            gen.writeString(userId.value().toString());
        } else {
            gen.writeNull();
        }
    }
}
