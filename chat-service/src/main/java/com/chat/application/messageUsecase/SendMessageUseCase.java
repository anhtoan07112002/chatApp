package com.chat.application.messageUsecase;

// import org.springframework.context.ApplicationEventPublisher;
// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Service;

import com.chat.application.DTO.SendMessageInput;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.User;
// import com.chat.domain.entity.user.UserId;
import com.chat.domain.service.messageservice.IMessageQueueService;
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
    private final IMessageQueueService messageQueueService;

    // private final KafkaTemplate<String, Message> kafkaTemplate;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1;

    public void execute(SendMessageInput input) {
        try {
            // Validate users
            User sender = userService.getUserById(input.getSenderId());
            User receiver = userService.getUserById(input.getReceiverId());

            if (sender == null || receiver == null) {
                throw new UserNotFoundException("Sender or receiver not found");
            }

            // Create message
            Message message = messageService.createMessage(
                sender.getId(),
                receiver.getId(),
                new MessageContent(input.getContent()),
                MessageType.TEXT
            );

            // Save initial message
            messageRepository.save(message);
            eventPublisher.publishMessageCreatedEvent(new MessageCreatedEvent(message));

            // Handle delivery based on receiver's status
            if (userService.isOnline(receiver.getId().asString())) {
                handleOnlineDelivery(message);
            } else {
                handleOfflineDelivery(message);
            }

        } catch (Exception e) {
            log.error("Error in SendMessageUseCase: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to process message", e);
        }
    }

    private void handleOnlineDelivery(Message message) {
        int retryCount = 0;
        long retryDelay = RETRY_DELAY_MS;
        String messageId = message.getId().toString();
        String recipientId = message.getReceiverId().toString();

        log.info("Starting online delivery for message: {} to recipient: {}", messageId, recipientId);

        while (retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                log.debug("Attempt {} - Sending message to recipient. Delay: {}ms", retryCount + 1, retryDelay);
                long startTime = System.currentTimeMillis();

                messageSender.sendMessage(message);

                long deliveryTime = System.currentTimeMillis() - startTime;
                log.info("Message {} successfully sent to recipient {} in {}ms", messageId, recipientId, deliveryTime);

                updateMessageStatus(message, MessageStatus.SENT);
                log.debug("Updated message status to SENT in database");

                eventPublisher.publishMessageSentEvent(new MessageSentEvent(message));
                log.debug("Published MessageSentEvent for message: {}", messageId);

                return;
            } catch (MessageDeliveryException e) {
                retryCount++;
                log.warn("Attempt {} failed to send message: {} - Error: {} - Stack trace: {}",
                        retryCount,
                        messageId,
                        e.getMessage(),
                        e.getStackTrace().length > 0 ? e.getStackTrace()[0] : "No stack trace");

                if (retryCount < MAX_RETRY_ATTEMPTS) {
                    try {
                        log.debug("Implementing exponential backoff - Waiting {}ms before retry", retryDelay);
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        log.error("Thread interrupted during retry delay for message: {}", messageId, ie);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Message delivery failed - MessageId: {}, Recipient: {}, Attempts: {}, Total time: {}ms",
                messageId,
                recipientId,
                MAX_RETRY_ATTEMPTS,
                retryDelay - RETRY_DELAY_MS); // Calculate total time based on accumulated delay

        log.info("Switching to offline delivery mode for message: {}", messageId);
        handleOfflineDelivery(message);
    }

    private void handleOfflineDelivery(Message message) {
        try {
            updateMessageStatus(message, MessageStatus.PENDING);
            messageQueueService.queueMessage(message);
            eventPublisher.publishMessageQueuedEvent(new MessageQueuedEvent(message));
        } catch (Exception e) {
            log.error("Failed to queue offline message: {}", e.getMessage(), e);
            updateMessageStatus(message, MessageStatus.FAILED);
            throw new MessageProcessingException("Failed to queue offline message", e);
        }
    }

    private void updateMessageStatus(Message message, MessageStatus status) {
        message.setStatus(status);
        messageRepository.save(message);
    }
}