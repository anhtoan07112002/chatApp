package com.chat.domain.service.messageservice;

import com.chat.domain.entity.messages.Message;

public interface IMessageSender {
    void sendMessage(Message message);
}