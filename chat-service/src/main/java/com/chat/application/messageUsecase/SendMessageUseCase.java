package com.chat.application.messageUsecase;

// import org.springframework.context.ApplicationEventPublisher;
// import org.springframework.kafka.core.KafkaTemplate;
import com.chat.config.kafka.KafkaProducer;
import com.chat.domain.entity.user.UserId;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Service;

import com.chat.application.DTO.SendMessageInput;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.User;
// import com.chat.domain.entity.user.UserId;
import com.chat.domain.service.messageservice.IMessageSender;
import com.chat.domain.service.messageservice.IMessageService;
import com.chat.domain.service.userservice.IUserService;
import com.chat.domain.event.IEventPublisher;
import com.chat.domain.event.messageEvent.MessageCreatedEvent;
import com.chat.domain.event.messageEvent.MessageQueuedEvent;
import com.chat.domain.event.messageEvent.MessageSentEvent;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.exception.userException.UserNotFoundException;
import com.chat.domain.exception.messageException.MessageProcessingException;
// import com.chat.domain.exception.messageException.InvalidMessageException;

import lombok.AllArgsConstructor;
// import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

// import lombok.Data;
// import lombok.Builder;


@Service
@Slf4j
@AllArgsConstructor
public class SendMessageUseCase {
    private final IMessageService messageService;
    private final IMessageSender messageSender;
    private final IUserService userService;
    private final IMessageRepository messageRepository;
    private final IEventPublisher eventPublisher;
    private final KafkaProducer kafkaProducer;
    private final MessageDeliveryCoordinator deliveryCoordinator;

    private static final String MESSAGE_TOPIC = "message-topic";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1;

    public void execute(SendMessageInput input) {
        try {
            validateUsers(input.getSenderId(), input.getReceiverId());
            Message message = createAndSaveMessage(input);

            // Try to deliver via WebSocket first
            if (userService.isOnline(input.getReceiverId())) {
                try {
                    deliverMessageViaWebSocket(message);
                } catch (MessageDeliveryException e) {
                    log.warn("WebSocket delivery failed, switching to Kafka: {}", e.getMessage());
                    handleKafkaDelivery(message);
                }
            } else {
                handleKafkaDelivery(message);
            }
        } catch (Exception e) {
            log.error("Error in SendMessageUseCase: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to process message", e);
        }
    }

    private void handleKafkaDelivery(Message message) {
        message.setStatus(MessageStatus.PENDING);
        messageRepository.save(message);
        kafkaProducer.sendMessage(MESSAGE_TOPIC, message);
        eventPublisher.publishMessageQueuedEvent(new MessageQueuedEvent(message));
    }

    private void deliverMessageViaWebSocket(Message message) {
        int retryCount = 0;
        long delay = RETRY_DELAY_MS;

        while (retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                deliveryCoordinator.sendMessage(message);
                message.setStatus(MessageStatus.SENT);
                message.setSentAt(LocalDateTime.now());
                messageRepository.save(message);
                eventPublisher.publishMessageSentEvent(new MessageSentEvent(message));
                return;
            } catch (MessageDeliveryException e) {
                retryCount++;
                if (retryCount == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                try {
                    Thread.sleep(delay);
                    delay *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new MessageDeliveryException("Delivery interrupted");
                }
            }
        }
    }

    private Message createAndSaveMessage(SendMessageInput input) {
        Message message = messageService.createMessage(
                String.valueOf(MessageType.TEXT),
                input.getSenderId(),
                input.getReceiverId(),
                new MessageContent(input.getContent()));

        message.setStatus(MessageStatus.CREATED);
        messageRepository.save(message);
        eventPublisher.publishMessageCreatedEvent(new MessageCreatedEvent(message));

        return message;
    }

    private void validateUsers(String senderId, String receiverId) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);

        if (sender == null || receiver == null) {
            throw new UserNotFoundException("Sender or receiver not found");
        }
    }
}