package com.chat.application.messageUsecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.service.messageservice.IMessageService;
import com.chat.domain.event.messageEvent.MessageCreatedEvent;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Data;
import lombok.Builder;


@Service
@Data
@AllArgsConstructor
@Builder
public class SendMessageUseCase {
    @NonNull
    private final IMessageService messageService;
    @NonNull
    private final ApplicationEventPublisher eventPublisher;

    public void execute(UserId senderId, UserId receiverId, MessageContent content) {
        Message message = messageService.sendMessage(senderId, receiverId, content);
        eventPublisher.publishEvent(new MessageCreatedEvent(message));
    }
}
