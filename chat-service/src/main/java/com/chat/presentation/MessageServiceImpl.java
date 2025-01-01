package com.chat.presentation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.service.messageservice.IMessageSender;
import com.chat.domain.service.messageservice.IMessageService;

@Service
public class MessageServiceImpl implements IMessageService {

    private final IMessageRepository messageRepository;
    private final IMessageSender messageSender;

    public MessageServiceImpl(IMessageRepository messageRepository, IMessageSender messageSender) {
        this.messageRepository = messageRepository;
        this.messageSender = messageSender;
    }

    @Override
    public Message sendMessage(UserId senderId, UserId receiverId, MessageContent content) {
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .status(MessageStatus.SENT)
                .build();

        // Lưu trữ message
        messageRepository.save(message);

        // Gửi message qua WebSocket
        messageSender.sendMessage(message);

        return message;
    }
}
