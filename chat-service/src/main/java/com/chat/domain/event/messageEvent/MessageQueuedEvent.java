package com.chat.domain.event.messageEvent;

import com.chat.domain.entity.messages.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageQueuedEvent {
    private final Message message;
}
