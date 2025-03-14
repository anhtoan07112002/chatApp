package com.chat.domain.entity.group;

import java.util.UUID;

public record GroupId(UUID id) {
    public GroupId {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    public GroupId(String id) {
        this(UUID.fromString(id));
    }

    public String asString() {
        return id.toString();
    }
}
