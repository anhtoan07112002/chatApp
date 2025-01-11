package com.chat.domain.entity.messages;

import java.util.UUID;

public record MessageId(UUID vaUuid) {
    public MessageId {
        if (vaUuid == null) {
            throw new IllegalArgumentException("MessageId cannot be null");
        }
    }     
}
