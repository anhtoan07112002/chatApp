package com.chat.application.messageUsecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.chat.application.DTO.SendMessageInput;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.service.messageservice.IMessageSender;
import com.chat.domain.service.messageservice.IMessageService;
import com.chat.domain.service.userservice.IUserService;
import com.chat.domain.event.EventPublisher;
import com.chat.domain.event.messageEvent.MessageCreatedEvent;
import com.chat.domain.event.messageEvent.MessageSentEvent;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Data;
import lombok.Builder;


@Service
@Data
@AllArgsConstructor
@Builder
public class SendMessageUseCase {
    private final IMessageService messageService;
    private final IMessageSender messageSender;
    private final IUserService userService;
    private final IMessageRepository messageRepository;
    private final EventPublisher eventPublisher;

    public void execute(SendMessageInput input) {
        User sender = userService.getUserById(input.getSenderId());
        User receiver = userService.getUserById(input.getReceiverId());

        Message message = messageService.creatMessage(
            sender.getId(), 
            receiver.getId(), 
            new MessageContent(input.getContent()), 
            MessageType.TEXT
        );

        messageRepository.save(message);
        eventPublisher.publishMessageCreatedEvent(new MessageCreatedEvent(message));

        if (userService.isOnline(receiver.getId())) {
            try {
                messageSender.sendMessage(message);
                message.markAsSent();
                eventPublisher.publishMessageSentEvent(new MessageSentEvent(message));
            } catch (Exception e) {
                // TODO: handle exception
                log.error("Failed to send message: {}", e.getMessage(), e);
                message.markAsPending();
            }
        } else {
            message.markAsPending();
        }
    }
}
