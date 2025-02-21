package com.chat.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import com.chat.domain.entity.messages.MessageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.messages.Message;
//import com.chat.domain.service.messageservice.IMessageQueueService;
import com.chat.domain.service.messageservice.IMessageSender;

@Component
@Slf4j
public class WebSocketMessageSender implements IMessageSender {
    @Lazy private final SimpMessagingTemplate template;
    private final WebSocketSessionManager sessionManager;
    private final Map<String, CompletableFuture<Void>> acknowledgmentMap = new ConcurrentHashMap<>();
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY = 1000;
    private static final long ACK_TIMEOUT_MS = 10000;

    public WebSocketMessageSender(SimpMessagingTemplate template,
                                  WebSocketSessionManager sessionManager) {
        this.template = template;
        this.sessionManager = sessionManager;
    }

    @Override
    public void sendMessage(Message message) {
        String receiverId = message.getReceiverId().asString();
        String messageId = message.getId().vaUuid().toString();

        if (!sessionManager.isUserOnline(receiverId)) {
            throw new MessageDeliveryException("User is offline");
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        acknowledgmentMap.put(messageId, future);

        try {
            template.convertAndSendToUser(
                    receiverId,
                    "/queue/messages",
                    message,
                    createMessageHeaders(messageId)
            );

            // Wait for acknowledgment
            future.get(ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());

        } catch (Exception e) {
            message.setStatus(MessageStatus.PENDING);
            throw new MessageDeliveryException("Failed to send message: " + e.getMessage());
        } finally {
            acknowledgmentMap.remove(messageId);
        }
    }

    private MessageHeaders createMessageHeaders(String messageId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("messageId", messageId);
        headers.put("requires-ack", true);
        return new MessageHeaders(headers);
    }

    public void handleAcknowledgment(String messageId) {
        CompletableFuture<Void> future = acknowledgmentMap.get(messageId);
        if (future != null) {
            future.complete(null);
            log.debug("Acknowledgment processed for message: {}", messageId);
        } else {
            log.warn("No acknowledgment future found for message: {}", messageId);
        }
    }
}
