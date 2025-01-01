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
    private MessageContent content;
    private LocalDateTime sentAt;
    private MessageStatus status;

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
}
