package com.chat.config.kafka.serializer;

import com.chat.domain.entity.user.UserId;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;

public class UserIdSerializer extends JsonSerializer<UserId> {
    @Override
    public void serialize(UserId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("@class", UserId.class.getName());
        gen.writeStringField("value", value.toString());
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(UserId value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) 
            throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }
}
