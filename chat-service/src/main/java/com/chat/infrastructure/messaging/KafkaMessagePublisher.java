package com.chat.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

// import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.service.messageservice.IMessagePublisher;
import com.chat.domain.exception.messageException.MessagePublishException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class KafkaMessagePublisher implements IMessagePublisher {
    private final KafkaTemplate<String, Message> kafkaTemplate;
    private static final String TOPIC = "message-topic";

    public KafkaMessagePublisher(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Message message) {
        try {
            CompletableFuture<SendResult<String, Message>> future =
                    kafkaTemplate.send(TOPIC, message.getId().toString(), message);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send message: {}", ex.getMessage(), ex);
                } else {
                    log.info("Message sent successfully: {}", message.getId());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing message: {}", e.getMessage(), e);
            throw new MessagePublishException("Error publishing message", e);
        }
    }
}