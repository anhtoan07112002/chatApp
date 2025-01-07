package com.chat.infrastructure.messaging;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.service.messageservice.IMessageQueueService;

@Service
public class KafkaMessageQueueService implements IMessageQueueService {
    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final IMessageRepository messageRepository;
    private static final String OFFLINE_MESSAGES_TOPIC = "offline-messages";

    public KafkaMessageQueueService(KafkaTemplate<String, Message> kafkaTemplate, IMessageRepository messageRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageRepository = messageRepository;
    }

    @Override
    public void queueMessage(Message message) {
        kafkaTemplate.send(OFFLINE_MESSAGES_TOPIC, message);
        message.setStatus(MessageStatus.PENDING);
        messageRepository.save(message);
    }

    @Override
    public List<Message> getPendingMessages(UserId userId) {
        return messageRepository.findPendingMessagesByReceiverId(userId);
    }

    @Override
    public void sendPendingMessages(UserId userId) {
        List<Message> pendingMessages = getPendingMessages(userId);
        for (Message message : pendingMessages) {
            kafkaTemplate.send(OFFLINE_MESSAGES_TOPIC, message);
            updateMessageStatus(message, MessageStatus.SENT);
        }
    }

    private void updateMessageStatus(Message message, MessageStatus status) {
        message.setStatus(status);
        messageRepository.save(message);
    }
}