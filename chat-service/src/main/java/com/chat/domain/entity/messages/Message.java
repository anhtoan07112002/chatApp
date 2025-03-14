package com.chat.domain.entity.messages;

import java.time.LocalDateTime;
import java.util.UUID;

// import org.springframework.data.mongodb.core.mapping.Document;

import com.chat.config.kafka.serializer.UserIdDeserializer;
import com.chat.config.kafka.serializer.UserIdSerializer;
import com.chat.domain.entity.user.UserId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
/* 
 * 
 * Message entity
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
// @Document(collection = "messages")
public class Message {
    private LocalDateTime receivedAt;
    private LocalDateTime readAt;
    @NonNull
    @Getter
    private MessageId id;
//    @JsonSerialize(using = UserIdSerializer.class)
//    @JsonDeserialize(using = UserIdDeserializer.class)
    private UserId senderId;
//    @JsonSerialize(using = UserIdSerializer.class)
//    @JsonDeserialize(using = UserIdDeserializer.class)
    private UserId receiverId;
    private MessageType type;
    private MessageContent content;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    @Setter
    private MessageStatus status;
    private LocalDateTime updatedAt;

    public Message() {

    }

    public void markAsRead() {
            this.setStatus(MessageStatus.READED);
        }
    
    public void markAsReceived() {
        this.setStatus(MessageStatus.RECEIVED);
    }

    public void markAsSent() {
        this.setStatus(MessageStatus.SENT);
    }

    public void markAsSeen() {
        this.setStatus(MessageStatus.SEEN);
    }

    public void markAsPending() {
        this.setStatus(MessageStatus.PENDING);
    }
    
    private Message(MessageId id, UserId senderId, UserId receiverId, MessageContent content, MessageType type) {
        if (id == null) {
            throw new IllegalArgumentException("MessageId cannot be null"); // Đảm bảo id không null
        }
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.status = MessageStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

//    @JsonCreator
//    public Message(
//            @JsonProperty("type") String type,
//            @JsonProperty("senderId") String senderId,
//            @JsonProperty("receiverId") String receiverId,
//            @JsonProperty("content") MessageContent content) {
//        this.id = new MessageId(UUID.randomUUID());
//        this.type = MessageType.valueOf(type);
//        this.senderId = UserId.fromString(senderId);
//        this.receiverId = UserId.fromString(receiverId);
//        this.content = content;
//        this.status = MessageStatus.CREATED;
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    public static Message create(UserId senderId, UserId receiverId, MessageContent content, MessageType type) {
//        MessageId id = new MessageId(UUID.randomUUID());
//        return new Message(id, senderId, receiverId, content, type);
//    }

    @JsonCreator
    public static Message create(
            @JsonProperty("type") String type,
            @JsonProperty("senderId") String senderId,
            @JsonProperty("receiverId") String receiverId,
            @JsonProperty("content") MessageContent content) {

        Message message = new Message();
        message.setId(new MessageId(UUID.randomUUID()));
        message.setType(MessageType.valueOf(type));
        message.setSenderId(UserId.fromString(senderId));
        message.setReceiverId(UserId.fromString(receiverId));
        message.setContent(content);
        message.setStatus(MessageStatus.CREATED);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        return message;
    }

    public static Message createFromExisting(
            String id,
            String type,
            String senderId,
            String receiverId,
            MessageContent content,
            MessageStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime sentAt,
            LocalDateTime receivedAt,
            LocalDateTime readAt) {

        Message message = new Message();
        message.setId(MessageId.fromString(id));  // Sử dụng ID có sẵn
        message.setType(MessageType.valueOf(type));
        message.setSenderId(UserId.fromString(senderId));
        message.setReceiverId(UserId.fromString(receiverId));
        message.setContent(content);
        message.setStatus(status);
        message.setCreatedAt(createdAt);
        message.setUpdatedAt(updatedAt);
        message.setSentAt(sentAt);
        message.setReceivedAt(receivedAt);
        message.setReadAt(readAt);

        return message;
    }
}
