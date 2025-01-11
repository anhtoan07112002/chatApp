package com.chat.domain.entity.blocklist;

import java.util.Set;

import com.chat.domain.entity.user.UserId;

public class BlockList {
    private BlockListId id;
    private UserId ownerId;
    private Set<Block> blocks;

    public BlockList(BlockListId id, UserId userId, Set<Block> blocks) {
        this.id = id;
        this.ownerId = userId;
        this.blocks = blocks;
    }

    public BlockListId getId() {
        return id;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UserId ownerId) {
        this.ownerId = ownerId;
    }

    public Set<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(Set<Block> blocks) {
        this.blocks = blocks;
    }
}
