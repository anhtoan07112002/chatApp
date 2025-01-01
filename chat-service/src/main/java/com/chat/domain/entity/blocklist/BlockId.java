package com.chat.domain.entity.blocklist;

import java.util.UUID;

public record BlockId(UUID value) {
    public BlockId {
        if (value == null) {
            throw new IllegalArgumentException("BlockId cannot be null");
        }
    }
}
