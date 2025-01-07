package com.chat.domain.service.messageservice;

import com.chat.domain.entity.messages.Message;

public interface IMessageListener {
    void onMessageReceived(Message message);
}
