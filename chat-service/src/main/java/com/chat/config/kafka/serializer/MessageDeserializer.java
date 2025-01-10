package com.chat.config.kafka.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class MessageDeserializer implements Deserializer<String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing Message", e);
        }
    }
}
