package com.chat.infrastructure.messaging;


import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Component;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.service.messageservice.IMessageListener;
import com.chat.domain.service.messageservice.IMessageSender;
import com.chat.domain.service.userservice.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MessageListenerImpl implements IMessageListener {
    private final IMessageSender messageSender;
    private final IUserService userService;
    private final ObjectProvider<IMessageRepository> messageRepositoryProvider;

    public MessageListenerImpl(
            IMessageSender messageSender,
            IUserService userService,
            ObjectProvider<IMessageRepository> messageRepositoryProvider) {
        this.messageSender = messageSender;
        this.userService = userService;
        this.messageRepositoryProvider = messageRepositoryProvider;
    }

    @Override
    public void onMessageReceived(Message message) {
        IMessageRepository messageRepository = messageRepositoryProvider.getIfAvailable();
        if (messageRepository == null) {
            log.error("Message repository not available");
            return;
        }

        try {
            log.info("Received message: {}", message.getId());
            String receiverId = message.getReceiverId().toString();

            if (userService.isOnline(receiverId)) {
                try {
                    messageSender.sendMessage(message);
                    message.setStatus(MessageStatus.SENT);
                    message.setSentAt(LocalDateTime.now());
                    messageRepository.save(message);
                    log.info("Message sent successfully to online user: {}", receiverId);
                } catch (MessageDeliveryException e) {
                    log.warn("Failed to deliver message, marking as pending: {}", e.getMessage());
                    message.setStatus(MessageStatus.PENDING);
                    messageRepository.save(message);
                }
            } else {
                message.setStatus(MessageStatus.PENDING);
                messageRepository.save(message);
                log.info("Receiver is offline, message marked as pending");
            }
        } catch (Exception e) {
            log.error("Error processing received message: {}", e.getMessage(), e);
            message.setStatus(MessageStatus.FAILED);
            messageRepository.save(message);
        }
    }
}