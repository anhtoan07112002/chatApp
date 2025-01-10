package com.chat.application.messageUsecase;

import org.springframework.stereotype.Service;
import com.chat.application.DTO.SendMessageInput;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.User;
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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


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
    private static final long RETRY_DELAY_MS = 1000;

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
        Exception lastException = null;

        while (retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                messageSender.sendMessage(message);
                updateMessageStatus(message, MessageStatus.SENT);
                eventPublisher.publishMessageSentEvent(new MessageSentEvent(message));
                return;
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.warn("Attempt {} failed to send message: {}", retryCount, e.getMessage());
                
                if (retryCount < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Failed to send message after {} attempts", MAX_RETRY_ATTEMPTS, lastException);
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