package com.chat.domain.entity.membership;

import java.util.UUID;

public record GroupMemberShipId(UUID id) {
    public GroupMemberShipId {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    public GroupMemberShipId(String id) {
        this(UUID.fromString(id));
    }

    public String asString() {
        return id.toString();
    }
    
}
