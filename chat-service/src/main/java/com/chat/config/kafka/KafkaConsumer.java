package com.chat.config.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.service.messageservice.IMessageListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaConsumer {

    private final IMessageListener messageListener;
    private final ObjectMapper objectMapper;
    private final int MAX_RETRY_ATTEMPTS = 3;
    private final KafkaProducer kafkaProducer;
    private final int RETRY_DELAY_MS = 1000;

    public KafkaConsumer(IMessageListener messageListener, ObjectMapper objectMapper, KafkaProducer kafkaProducer) {
        this.messageListener = messageListener;
        this.objectMapper = objectMapper;
        this.kafkaProducer = kafkaProducer;
    }

    @KafkaListener(topics = "${kafka.topic.message}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String messageJson, Acknowledgment ack) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                Message message = objectMapper.readValue(messageJson, Message.class);
                messageListener.onMessageReceived(message);
                ack.acknowledge();
                log.info("Message processed successfully: {}", message.getId());
                return;
            } catch (Exception e) {
                retryCount++;
                log.error("Error processing message (attempt {}/{}): {}", 
                    retryCount, MAX_RETRY_ATTEMPTS, e.getMessage());
                if (retryCount == MAX_RETRY_ATTEMPTS) {
                    // Move to dead letter queue
                    Message message;
                    try {
                        message = objectMapper.readValue(messageJson, Message.class);
                        kafkaProducer.sendMessage("offline-messages", message);
                        ack.acknowledge();
                    } catch (JsonMappingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (JsonProcessingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    
                }
                sleep(RETRY_DELAY_MS * retryCount);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}