package com.chat.config.kafka;

import com.chat.domain.entity.messages.Message;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, Message> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Message message) {
        try {
            kafkaTemplate.send(topic, message);
            log.info("Message sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
        }
    }
}