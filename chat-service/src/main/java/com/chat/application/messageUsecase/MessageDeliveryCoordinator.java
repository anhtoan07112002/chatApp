package com.chat.application.messageUsecase;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageDeliveryCoordinator {
    private final WebSocketSessionManager sessionManager;
    private final IMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageDeliveryCoordinator(
            WebSocketSessionManager sessionManager,
            IMessageRepository messageRepository,
            @Lazy SimpMessagingTemplate messagingTemplate) {
        this.sessionManager = sessionManager;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // In MessageDeliveryCoordinator.java
    public void sendMessage(Message message) {
        if (message == null || message.getReceiverId() == null) {
            log.error("Cannot send null message or message with null receiverId");
            throw new MessageDeliveryException("Invalid message or receiver");
        }

        String receiverId = message.getReceiverId().asString();

        if (!sessionManager.isUserOnline(receiverId)) {
            log.debug("User {} is not online, message will be queued", receiverId);
            throw new MessageDeliveryException("User is offline");
        }

        try {
            log.debug("Attempting to send message {} to user {}", message.getId(), receiverId);
            messagingTemplate.convertAndSendToUser(
                    receiverId,
                    "/queue/messages",
                    message
            );
            log.debug("Successfully sent message {} to user {}", message.getId(), receiverId);
        } catch (Exception e) {
            log.error("Failed to send message {} to user {}: {}", message.getId(), receiverId, e.getMessage());
            throw new MessageDeliveryException("Failed to send message: " + e.getMessage());
        }
    }

    public void deliverPendingMessage(UserId userId) {
        if (userId == null) {
            log.error("Cannot deliver messages for null userId");
            return;
        }

        String sessionId = sessionManager.getSession(userId);
        if (sessionId == null) {
            log.debug("No active session found for userId: {}", userId);
            return;
        }

        List<Message> pendingMessages = messageRepository.findPendingMessagesByReceiverId(userId);
        log.debug("Found {} pending messages for userId: {}", pendingMessages.size(), userId);

        for (Message message : pendingMessages) {
            try {
                if (!sessionManager.isUserOnline(userId.asString())) {
                    log.debug("User {} went offline during message delivery", userId);
                    break;
                }

                messagingTemplate.convertAndSendToUser(
                        message.getReceiverId().asString(),
                        "/queue/messages",
                        message
                );

                message.setStatus(MessageStatus.SENT);
                messageRepository.save(message);
                log.debug("Successfully delivered message {} to user {}", message.getId(), userId);
            } catch (IllegalStateException e) {
                log.warn("Session closed while sending message {} to user {}", message.getId(), userId);
                // Don't update message status - it will remain pending
                break;
            } catch (Exception e) {
                log.error("Failed to send pending message {} to user {}: {}",
                        message.getId(), userId, e.getMessage());
                message.setStatus(MessageStatus.FAILED);
                messageRepository.save(message);
            }
        }
    }
}