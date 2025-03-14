package com.chat.domain.service.messageservice;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.UserId;

public interface IMessageService {
    Message createMessage(String type, String senderId, String receiverId, MessageContent content); // Create a message
    void save(Message message);
}
