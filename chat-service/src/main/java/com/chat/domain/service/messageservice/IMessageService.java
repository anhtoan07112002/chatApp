package com.chat.domain.service.messageservice;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.messages.MessageType;
import com.chat.domain.entity.user.UserId;

public interface IMessageService {
    Message createMessage(UserId senderId, UserId receiverId, MessageContent content, MessageType type); // Create a message
    void save(Message message);
}
