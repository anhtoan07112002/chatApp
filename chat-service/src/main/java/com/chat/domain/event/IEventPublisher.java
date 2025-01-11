package com.chat.domain.event;

import com.chat.domain.event.messageEvent.MessageCreatedEvent;
import com.chat.domain.event.messageEvent.MessageQueuedEvent;
import com.chat.domain.event.messageEvent.MessageSentEvent;

public interface IEventPublisher {
    void publishMessageCreatedEvent(MessageCreatedEvent event);
    void publishMessageSentEvent(MessageSentEvent event);
    void publishMessageQueuedEvent(MessageQueuedEvent event);
}
