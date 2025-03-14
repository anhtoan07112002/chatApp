package com.chat.domain.entity.blocklist;

import java.util.UUID;

public record BlockListId(UUID value) {
    public BlockListId {
        if (value == null) {
            throw new IllegalArgumentException("BlockListId cannot be null");
        }
    }   
}
