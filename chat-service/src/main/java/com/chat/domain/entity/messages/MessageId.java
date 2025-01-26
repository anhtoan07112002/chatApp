package com.chat.domain.entity.messages;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;

public record MessageId(UUID vaUuid) {
    public MessageId {
        if (vaUuid == null) {
            throw new IllegalArgumentException("MessageId cannot be null");
        }
    }

    @JsonCreator
    public static MessageId fromString(String id) {
        return new MessageId(UUID.fromString(id));
    }
}