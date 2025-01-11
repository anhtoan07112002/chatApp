package com.chat.infrastructure.messaging;

import org.springframework.stereotype.Service;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.service.messageservice.IMessageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {
    private final IMessageRepository messageRepository;

    @Override
    public Message createMessage(UserId senderId, UserId receiverId, MessageContent content, MessageType type) {
        return Message.create(senderId, receiverId, content, type);
    }

    @Override
    public void save(Message message) {
        messageRepository.save(message);
    }
}
