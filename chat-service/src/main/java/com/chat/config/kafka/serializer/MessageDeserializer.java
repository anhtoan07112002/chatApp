package com.chat.config.kafka.serializer;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.user.UserId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class MessageDeserializer extends JsonDeserializer<Message> {
    public MessageDeserializer() {
        super(new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SimpleModule()
                        .addDeserializer(UserId.class, new UserIdDeserializer())
                ));
        this.addTrustedPackages("*");
    }
}
