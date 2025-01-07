package com.chat.domain.entity.user;
import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }
    }

    public String asString() {
        return value.toString();
    }
}