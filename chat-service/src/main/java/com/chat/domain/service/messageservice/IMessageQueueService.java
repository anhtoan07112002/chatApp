package com.chat.domain.service.messageservice;

import java.util.List;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.user.UserId;

public interface IMessageQueueService {
    void queueMessage(Message message);
    List<Message> getPendingMessages(UserId userId);
    void sendPendingMessages(UserId userId);
    // void updateMessageStatus(Message message, MessageStatus status);
}