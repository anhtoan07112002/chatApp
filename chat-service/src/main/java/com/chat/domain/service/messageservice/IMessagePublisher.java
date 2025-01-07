package com.chat.domain.service.messageservice;

import com.chat.domain.entity.messages.Message;

public interface IMessagePublisher {
    void publish(Message message); // Message là domain object của bạn
}
