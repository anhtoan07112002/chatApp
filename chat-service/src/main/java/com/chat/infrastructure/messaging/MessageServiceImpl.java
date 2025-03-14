package com.chat.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.service.messageservice.IMessageService;

import lombok.RequiredArgsConstructor;

@Service
@Slf4j
public class MessageServiceImpl implements IMessageService {
    private final ObjectProvider<IMessageRepository> messageRepositoryProvider;

    public MessageServiceImpl(ObjectProvider<IMessageRepository> messageRepositoryProvider) {
        this.messageRepositoryProvider = messageRepositoryProvider;
    }

    @Override
    public Message createMessage(String type, String senderId, String receiverId, MessageContent content) {
        return Message.create(type, senderId, receiverId, content);
    }

    @Override
    public void save(Message message) {
        IMessageRepository messageRepository = messageRepositoryProvider.getIfAvailable();
        if (messageRepository == null) {
            log.error("Message repository not available");
            throw new IllegalStateException("Message repository not available");
        }
        messageRepository.save(message);
    }
}
