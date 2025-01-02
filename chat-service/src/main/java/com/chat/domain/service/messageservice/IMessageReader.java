package com.chat.domain.service.messageservice;

// import java.util.List;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageId;
// import com.chat.domain.entity.user.UserId;

public interface IMessageReader {
    Message getMessageById(MessageId messageId);
    // List<Message> getMessagesBetween(UserId user1, UserId user2, int limit);
}
