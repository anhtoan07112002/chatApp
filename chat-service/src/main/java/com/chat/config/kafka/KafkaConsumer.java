package com.chat.config.kafka;

//import com.chat.domain.service.messageservice.IMessageQueueService;
import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.event.IEventPublisher;
import com.chat.domain.event.messageEvent.MessageSentEvent;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.service.messageservice.IMessageListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Component
@Slf4j
//public class KafkaConsumer {
//
//    private final IMessageListener messageListener;
//    private final ObjectMapper objectMapper;
////    private IMessageQueueService messageQueueService;
//    private final int MAX_RETRY_ATTEMPTS = 3;
//    private final int RETRY_DELAY_MS = 1000;
//
//    public KafkaConsumer(IMessageListener messageListener, ObjectMapper objectMapper)
////    IMessageQueueService messageQueueService)
//    {
//        this.messageListener = messageListener;
//        this.objectMapper = objectMapper;
////        this.messageQueueService = messageQueueService;
//    }
//
//    @KafkaListener(topics = "${kafka.topic.message}", groupId = "${spring.kafka.consumer.group-id}")
//    public void listen(Message message, Acknowledgment ack) {
////        Message message = null;
//        try {
////            message = objectMapper.readValue(messageJson, Message.class);
//            processMessage(message);
//            ack.acknowledge();
//        } catch (Exception e) {
//            handleMessageError(message, null, e);
//            ack.acknowledge();
//        }
//    }
//    private void processMessage(Message message) {
//        int retryCount = 0;
//        while (retryCount < MAX_RETRY_ATTEMPTS) {
//            try {
//                messageListener.onMessageReceived(message);
//                log.info("Message processed successfully: {}", message.getId());
//                return;
//            } catch (Exception e) {
//                retryCount++;
//                log.error("Error processing message (attempt {}/{}): {}",
//                        retryCount, MAX_RETRY_ATTEMPTS, e.getMessage());
//
//                if (retryCount < MAX_RETRY_ATTEMPTS) {
//                    sleep(RETRY_DELAY_MS * retryCount);
//                } else {
//                    // Move to offline queue on final retry
////                    messageQueueService.queueMessage(message);
//                    log.info("Message moved to offline queue: {}", message.getId());
//                    throw e;
//                }
//            }
//        }
//    }
//
//    private void handleMessageError(Message message, String messageJson, Exception e) {
//        String messageId = message != null ? message.getId().toString() : "unknown";
//        log.error("Failed to process message {}: {}", messageId, e.getMessage());
//
//        try {
//            // Ensure message is queued for retry
//            if (message != null) {
////                messageQueueService.queueMessage(message);
//                log.info("Message {} queued for retry", messageId);
//            }
//        } catch (Exception queueError) {
//            log.error("Failed to queue message for retry: {}", queueError.getMessage());
//        }
//    }
//
//    private void sleep(long millis) {
//        try {
//            Thread.sleep(millis);
//        } catch (InterruptedException ie) {
//            Thread.currentThread().interrupt();
//        }
//    }
//}

public class KafkaConsumer {
    private final IMessageListener messageListener;
    private final WebSocketSessionManager sessionManager;
    private final IMessageRepository messageRepository;
    private final IEventPublisher eventPublisher;
    private final KafkaTemplate<String, Message> kafkaTemplate;

    public KafkaConsumer(
            IMessageListener messageListener,
            WebSocketSessionManager sessionManager,
            IMessageRepository messageRepository,
            IEventPublisher eventPublisher,
            KafkaTemplate<String, Message> kafkaTemplate) {
        this.messageListener = messageListener;
        this.sessionManager = sessionManager;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${kafka.topic.message}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Message message, Acknowledgment ack) {
        try {
            log.info("Message received from kafka: {}", message.getId());

            // Lấy tin nhắn hiện tại từ MongoDB
            Message existingMessage = messageRepository.findById(message.getId());
            if (existingMessage == null) {
                log.error("Message {} not found in database", message.getId());
                ack.acknowledge();
                return;
            }

            String receiverId = existingMessage.getReceiverId().asString();

            // Nếu tin nhắn đã được gửi, bỏ qua
            if (existingMessage.getStatus() == MessageStatus.SENT) {
                log.debug("Message {} already sent, skipping", message.getId());
                ack.acknowledge();
                return;
            }

            // Kiểm tra người nhận có online không
            if (sessionManager.isUserOnline(receiverId)) {
                try {
                    // Gửi tin nhắn qua WebSocket
                    messageListener.onMessageReceived(existingMessage);

                    // Cập nhật trạng thái trong MongoDB
                    existingMessage.setStatus(MessageStatus.SENT);
                    existingMessage.setSentAt(LocalDateTime.now());
                    messageRepository.save(existingMessage);

                    // Publish event
                    eventPublisher.publishMessageSentEvent(new MessageSentEvent(existingMessage));

                    // Cập nhật tin nhắn trong Kafka
//                    kafkaTemplate.send("message-topic", existingMessage.getId().vaUuid().toString(), existingMessage);

                    log.info("Message delivered successfully: {}", message.getId());
                } catch (Exception e) {
                    log.error("Error delivering message {}: {}", message.getId(), e.getMessage());
                    existingMessage.setStatus(MessageStatus.PENDING);
                    messageRepository.save(existingMessage);
                }
            } else {
                if (existingMessage.getStatus() != MessageStatus.PENDING) {
                    existingMessage.setStatus(MessageStatus.PENDING);
                    messageRepository.save(existingMessage);
                }
                log.debug("User {} is offline, keeping message {} as PENDING\", receiverId, message.getId()");
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing message from kafka: {}", e.getMessage(), e);
            ack.acknowledge(); // Acknowledge anyway to prevent infinite retries
        }
    }
}
