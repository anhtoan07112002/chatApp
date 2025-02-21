package com.chat.infrastructure.persistence.messsage.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.chat.domain.entity.messages.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "messages")
public class MongoMessageEntity {
    @Id
    private String id;
    private String senderId;
    private String receiverId;
    private String content;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private LocalDateTime readAt;

    public static MongoMessageEntity fromDomain(Message message) {
        MongoMessageEntity entity = new MongoMessageEntity();
        entity.setId(message.getId().vaUuid().toString());
        entity.setSenderId(message.getSenderId().asString());
        entity.setReceiverId(message.getReceiverId().asString());
        entity.setContent(message.getContent().getContent());
        entity.setType(message.getType().name());
        entity.setStatus(message.getStatus().name());
        entity.setCreatedAt(message.getCreatedAt());
        entity.setUpdatedAt(message.getUpdatedAt());
        entity.setSentAt(message.getSentAt());
        entity.setReceivedAt(message.getReceivedAt());
        entity.setReadAt(message.getReadAt());
        return entity;
    }

    public Message toDomain() {
        return Message.createFromExisting(
                this.id,                                    // Sử dụng ID có sẵn
                this.type,
                this.senderId,
                this.receiverId,
                new MessageContent(this.content),
                MessageStatus.valueOf(this.status),
                this.createdAt,
                this.updatedAt,
                this.sentAt,
                this.receivedAt,
                this.readAt
        );
    }
}
