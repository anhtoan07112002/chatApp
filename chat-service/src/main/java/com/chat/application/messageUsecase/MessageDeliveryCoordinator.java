package com.chat.application.messageUsecase;

import java.time.LocalDateTime;
import java.util.List;

import com.chat.application.DTO.MessageResponse;
import com.chat.domain.event.IEventPublisher;
import com.chat.domain.event.messageEvent.MessageSentEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final ObjectProvider<IMessageRepository> messageRepositoryProvider;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;
    private final IEventPublisher eventPublisher;
    private final KafkaTemplate<String, Message> kafkaTemplate;

    public MessageDeliveryCoordinator(
            WebSocketSessionManager sessionManager,
            ObjectProvider<IMessageRepository> messageRepositoryProvider,
            ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider,
            IEventPublisher eventPublisher,
            KafkaTemplate<String, Message> kafkaTemplate) {
        this.sessionManager = sessionManager;
        this.messageRepositoryProvider = messageRepositoryProvider;
        this.messagingTemplateProvider = messagingTemplateProvider;
        this.eventPublisher = eventPublisher;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void deliverPendingMessages(UserId userId) {
        if (userId == null) {
            log.error("Cannot deliver messages for null userId");
            return;
        }

        IMessageRepository messageRepository = messageRepositoryProvider.getIfAvailable();
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();

        if (messageRepository == null || messagingTemplate == null) {
            log.error("Required dependencies not available");
            return;
        }

        if (!sessionManager.isUserOnline(userId.toString())) {
            log.debug("User {} is not online, skipping pending message delivery", userId);
            return;
        }

        List<Message> pendingMessages = messageRepository.findPendingMessagesByReceiverId(userId);
        log.debug("Found {} pending messages for userId: {}", pendingMessages.size(), userId);

        for (Message message : pendingMessages) {
            try {
                log.debug("Attempting to deliver pending message: {}", message.getId());

                MessageResponse response = MessageResponse.builder()
                        .id(MessageResponse.MessageId.builder()
                                .vaUuid(message.getId().vaUuid().toString())
                                .build())
                        .senderId(message.getSenderId().toString())
                        .receiverId(message.getReceiverId().toString())
                        .content(message.getContent().getContent())
                        .status(MessageStatus.SENT.name())
                        .timestamp(LocalDateTime.now().toString())
                        .build();
                // Gửi tin nhắn qua WebSocket

                log.debug("Sending WebSocket message: {}", response);

                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/messages",
                        response
                );

                message.setStatus(MessageStatus.SENT);
                message.setSentAt(LocalDateTime.now());
                messageRepository.save(message);

                eventPublisher.publishMessageSentEvent(new MessageSentEvent(message));

                kafkaTemplate.send("message-topic", message.getId().vaUuid().toString(), message)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to update message status in Kafka: {}", ex.getMessage());
                            } else {
                                log.debug("Message status updated in Kafka for message: {}", message.getId());
                            }
                        });
                log.debug("Successfully delivered pending message {} to user {}", message.getId(), userId);

            } catch (Exception e) {
                log.error("Failed to send pending message {} to user {}: {}",
                        message.getId(), userId, e.getMessage());
            }
        }
    }

    public void sendMessage(Message message) {
        if (message == null || message.getReceiverId() == null) {
            log.error("Cannot send null message or message with null receiverId");
            throw new MessageDeliveryException("Invalid message or receiver");
        }

        String receiverId = message.getReceiverId().toString();
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();

        if (messagingTemplate == null) {
            throw new MessageDeliveryException("Messaging template not available");
        }

        if (!sessionManager.isUserOnline(receiverId)) {
            log.debug("User {} is not online, message will be queued", receiverId);
            message.setStatus(MessageStatus.PENDING);
            throw new MessageDeliveryException("User is offline");
        }

        try {
            log.debug("Attempting to send message {} to user {}", message.getId(), receiverId);

            MessageResponse response = MessageResponse.builder()
                    .id(MessageResponse.MessageId.builder().vaUuid(message.getId().vaUuid().toString()).build())
                    .senderId(message.getSenderId().toString())
                    .receiverId(message.getReceiverId().toString())
                    .content(message.getContent().getContent())
                    .status(message.getStatus().name())
                    .timestamp(LocalDateTime.now().toString())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    receiverId,
                    "/queue/messages",
                    response
            );

            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());

            log.debug("Successfully sent message {} to user {}", message.getId(), receiverId);
        } catch (Exception e) {
            log.error("Failed to send message {} to user {}: {}",
                    message.getId(), receiverId, e.getMessage());
            message.setStatus(MessageStatus.PENDING);
            throw new MessageDeliveryException("Failed to send message: " + e.getMessage());
        }
    }
}