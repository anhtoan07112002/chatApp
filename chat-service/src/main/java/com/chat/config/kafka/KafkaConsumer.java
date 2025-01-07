package com.chat.config.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.service.messageservice.IMessageListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaConsumer {

    private final IMessageListener messageListener;
    private final ObjectMapper objectMapper;

    public KafkaConsumer(IMessageListener messageListener, ObjectMapper objectMapper) {
        this.messageListener = messageListener;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topic.message}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String messageJson, Acknowledgment ack) {
        try {
            Message message = objectMapper.readValue(messageJson, Message.class);
            messageListener.onMessageReceived(message);
            ack.acknowledge();
            log.info("Message processed successfully: {}", message.getId());
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            // Có thể thêm logic retry hoặc dead letter queue ở đây
        }
    }
}