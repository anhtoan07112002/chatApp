package com.chat.domain.entity.blocklist;

import java.time.LocalDateTime;

import com.chat.domain.entity.user.UserId;

/**
 * The Block.
 */
class Block {
    private BlockId id;
    private UserId blockerId;
    private LocalDateTime blockedAt;
    private LocalDateTime unblockedAt;
    private String reason;
    private BlockType type;

    /**
     * Instantiates a new Block.
     *
     * @param id         the id
     * @param blockerId  the blocker id
     * @param blockedAt  the blocked at
     * @param unblockedAt the unblocked at
     * @param reason     the reason
     * @param type       the type
     */
    public Block(BlockId id, UserId blockerId, LocalDateTime blockedAt, LocalDateTime unblockedAt, String reason, BlockType type) {
        this.id = id;
        this.blockerId = blockerId;
        this.blockedAt = blockedAt;
        this.unblockedAt = unblockedAt;
        this.reason = reason;
        this.type = type;
    }

    public BlockId getId() {
        return id;
    }

    public UserId getBlockerId() {
        return blockerId;
    }

    public void setBlockerId(UserId blockerId) {
        this.blockerId = blockerId;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }

    public LocalDateTime getUnblockedAt() {
        return unblockedAt;
    }

    public void setUnblockedAt(LocalDateTime unblockedAt) {
        this.unblockedAt = unblockedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BlockType getType() {
        return type;
    }

    public void setType(BlockType type) {
        this.type = type;
    }

    
}