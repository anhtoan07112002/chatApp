package com.chat.infrastructure.messaging;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageId;
import com.chat.domain.service.messageservice.IMessageQueueService;
import com.chat.domain.service.messageservice.IMessageSender;

@Component
public class WebSocketMessageSender implements IMessageSender {
    @Lazy private final SimpMessagingTemplate template;
    private final WebSocketSessionManager sessionManager;
    private final IMessageQueueService messageQueueService;
    private final Map<String, CompletableFuture<Void>> acknowledgmentMap = new ConcurrentHashMap<>();
    private static final long ACK_TIMEOUT_MS = 5000;

    public WebSocketMessageSender(SimpMessagingTemplate template, 
                                WebSocketSessionManager sessionManager, IMessageQueueService messageQueueService) {
        this.template = template;
        this.sessionManager = sessionManager;
        this.messageQueueService = messageQueueService;
    }

    @Override
    public void sendMessage(Message message) {
        if (!sessionManager.isUserOnline(message.getReceiverId().asString())) {
            messageQueueService.queueMessage(message);
            throw new MessageDeliveryException("User is offline");
        }

        try {
            template.convertAndSendToUser(
                message.getReceiverId().asString(),
                "/queue/messages",
                message
            );
            waitForAcknowledgment(message.getId());
        } catch (Exception e) {
            throw new MessageDeliveryException("Failed to send message: " + e.getMessage());
        }
    }

    private void waitForAcknowledgment(MessageId messageId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String messageIdStr = messageId.toString();
        acknowledgmentMap.put(messageIdStr, future);

        try {
            // Chờ tối đa ACK_TIMEOUT_MS để nhận acknowledgment
            future.get(ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            acknowledgmentMap.remove(messageId);
        } catch (TimeoutException e) {
            acknowledgmentMap.remove(messageId);
            throw new MessageDeliveryException("Acknowledgment timed out for message: " + messageId);
        } catch (Exception e) {
            acknowledgmentMap.remove(messageId);
            throw new MessageDeliveryException("Error while waiting for acknowledgment: " + e.getMessage());
        }
    }

    // Xử lý acknowledgment từ client
    public void handleAcknowledgment(String messageId) {
        CompletableFuture<Void> future = acknowledgmentMap.get(messageId);
        if (future != null) {
            future.complete(null);
        }
    }
}
