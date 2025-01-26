package com.chat.infrastructure.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.service.messageservice.IMessageQueueService;
import com.chat.domain.service.messageservice.IMessageSender;

@Component
@Slf4j
public class WebSocketMessageSender implements IMessageSender {
    @Lazy private final SimpMessagingTemplate template;
    private final WebSocketSessionManager sessionManager;
    private final IMessageQueueService messageQueueService;
    private final Map<String, CompletableFuture<Void>> acknowledgmentMap = new ConcurrentHashMap<>();
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY = 1000;
    private static final long ACK_TIMEOUT_MS = 10000;

    public WebSocketMessageSender(SimpMessagingTemplate template, 
                                WebSocketSessionManager sessionManager, IMessageQueueService messageQueueService) {
        this.template = template;
        this.sessionManager = sessionManager;
        this.messageQueueService = messageQueueService;
    }

    @Override
    public void sendMessage(Message message) {
        String receiverId = message.getReceiverId().asString();
        String messageId = message.getId().vaUuid().toString();
        String senderId = message.getSenderId().asString();

        log.info("Attempting to send message: [ID: {}, From: {}, To: {}]",
                messageId, senderId, receiverId);

        // Check user online status
        if (!sessionManager.isUserOnline(receiverId)) {
            log.warn("Recipient {} is offline. Queuing message {} for later delivery",
                    receiverId, messageId);
            messageQueueService.queueMessage(message);
            throw new MessageDeliveryException("User is offline");
        }

        log.debug("Recipient {} is online, proceeding with message delivery", receiverId);

        CompletableFuture<Void> future = new CompletableFuture<>();
        acknowledgmentMap.put(messageId, future);
        log.debug("Created acknowledgment future for message: {}", messageId);

        long startTime = System.currentTimeMillis();
        try {
            log.debug("Preparing to send message {} to user destination: /user/{}/queue/messages",
                    messageId, receiverId);

            MessageHeaders headers = createMessageHeaders(messageId);
            log.debug("Created message headers: {}", headers);

            template.convertAndSendToUser(
                    receiverId,
                    "/queue/messages",
                    message,
                    headers
            );

            log.debug("Message {} sent to broker, waiting for acknowledgment (timeout: {}ms)",
                    messageId, ACK_TIMEOUT_MS);

            // Wait for acknowledgment
            future.get(ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            long deliveryTime = System.currentTimeMillis() - startTime;
            log.info("Message {} successfully delivered to {} in {}ms",
                    messageId, receiverId, deliveryTime);

        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for message {} acknowledgment", messageId, e);
            Thread.currentThread().interrupt();
            throw new MessageDeliveryException("Message delivery interrupted: " + e.getMessage());

        } catch (TimeoutException e) {
            log.error("Acknowledgment timeout for message {} after {}ms",
                    messageId, ACK_TIMEOUT_MS);
            throw new MessageDeliveryException("Message acknowledgment timeout after " + ACK_TIMEOUT_MS + "ms");

        } catch (ExecutionException e) {
            log.error("Error occurred while sending message {}: {}",
                    messageId, e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new MessageDeliveryException("Message delivery failed: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error while sending message {}: {}",
                    messageId, e.getMessage(), e);
            throw new MessageDeliveryException("Failed to send message: " + e.getMessage());

        } finally {
            if (acknowledgmentMap.remove(messageId) != null) {
                log.debug("Cleaned up acknowledgment future for message: {}", messageId);
            }
        }
    }

    private MessageHeaders createMessageHeaders(String messageId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("messageId", messageId);
        headers.put("requires-ack", true);
        return new MessageHeaders(headers);
    }

    public void handleAcknowledgment(String messageId) {
        log.debug("Processing acknowledgment for message: {}", messageId);
        CompletableFuture<Void> future = acknowledgmentMap.get(messageId);
        if (future != null) {
            log.debug("Found acknowledgment future for message: {}", messageId);
            future.complete(null);
        } else {
            log.warn("No acknowledgment future found for message: {}", messageId);
        }
    }
}
