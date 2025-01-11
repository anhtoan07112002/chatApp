package com.chat.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

// import org.springframework.kafka.annotation.KafkaListener;
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

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "message-topic";

    public KafkaMessagePublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(Message message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            CompletableFuture<org.springframework.kafka.support.SendResult<String, String>> future =
                    kafkaTemplate.send(TOPIC, message.getId().toString(), messageJson);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send message: {}", ex.getMessage(), ex);
                } else {
                    log.info("Message sent successfully: {}", message.getId());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Error serializing message: {}", e.getMessage(), e);
            throw new MessagePublishException("Error serializing message", e);
        }
    }
}