package com.chat.infrastructure.messaging;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.exception.messageException.MessagePublishException;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.service.messageservice.IMessageQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaMessageQueueService implements IMessageQueueService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IMessageRepository messageRepository;
    private static final String OFFLINE_MESSAGES_TOPIC = "offline-messages";
    private final ObjectMapper objectMapper;


    public KafkaMessageQueueService(KafkaTemplate<String, String> kafkaTemplate, IMessageRepository messageRepository, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void queueMessage(Message message) {
        
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(OFFLINE_MESSAGES_TOPIC, messageJson);
            message.setStatus(MessageStatus.PENDING);
            messageRepository.save(message);
        } catch (Exception e) {
            // TODO: handle exception
            log.error("Error serializing message: {}", e.getMessage(), e);
            throw new MessagePublishException("Error serializing message", e);
        }
    }

    @Override
    public List<Message> getPendingMessages(UserId userId) {
        return messageRepository.findPendingMessagesByReceiverId(userId);
    }

    @Override
    public void sendPendingMessages(UserId userId) {
        List<Message> pendingMessages = getPendingMessages(userId);
        try {
            for (Message message : pendingMessages) {
                String messageJson = objectMapper.writeValueAsString(message);
                kafkaTemplate.send(OFFLINE_MESSAGES_TOPIC, messageJson);
                updateMessageStatus(message, MessageStatus.SENT);
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error("Error serializing message: {}", e.getMessage(), e);
            throw new MessagePublishException("Error serializing message", e);
        }
        
    }

    private void updateMessageStatus(Message message, MessageStatus status) {
        message.setStatus(status);
        messageRepository.save(message);
    }
}