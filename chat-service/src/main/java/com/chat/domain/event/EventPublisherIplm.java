package com.chat.domain.event;

import org.springframework.context.ApplicationEventPublisher;
// import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.chat.domain.event.messageEvent.MessageCreatedEvent;
import com.chat.domain.event.messageEvent.MessageQueuedEvent;
import com.chat.domain.event.messageEvent.MessageSentEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventPublisherIplm implements IEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public EventPublisherIplm(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishMessageSentEvent(MessageSentEvent event) {
        applicationEventPublisher.publishEvent(event);
        log.info("Message sent successfully: {}", event.getMessage());
        // throw new UnsupportedOperationException("Unimplemented method 'publishMessageSentEvent'");
    }

    @Override
    public void publishMessageQueuedEvent(MessageQueuedEvent event) {
        applicationEventPublisher.publishEvent(event);
        log.info("Message queued successfully: {}", event.getMessage());
        // throw new UnsupportedOperationException("Unimplemented method 'publishMessageQueuedEvent'");
    }

    @Override
    public void publishMessageCreatedEvent(MessageCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
        log.info("Message created successfully: {}", event.getMessage());
        // throw new UnsupportedOperationException("Unimplemented method 'publishMessageCreatedEvent'");
    }

    
}
