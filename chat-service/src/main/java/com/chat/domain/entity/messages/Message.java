package com.chat.domain.entity.messages;

import java.time.LocalDateTime;

// import org.springframework.data.mongodb.core.mapping.Document;

import com.chat.domain.entity.user.UserId;

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
    private UserId senderId;
    private UserId receiverId;
    private MessageType type;
    private MessageContent content;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    @Setter
    private MessageStatus status;
    private LocalDateTime updatedAt;
    
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
        
        private Message(UserId senderId, UserId receiverId, MessageContent content, MessageType type) {
            this.id = new MessageId(null);
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.content = content;
            this.type = type;
            this.status = MessageStatus.CREATED;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
    }

    public static Message create(UserId senderId, UserId receiverId, MessageContent content, MessageType type) {
        return new Message(senderId, receiverId, content, type);
    }
}
