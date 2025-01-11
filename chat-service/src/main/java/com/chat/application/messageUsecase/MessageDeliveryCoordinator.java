package com.chat.application.messageUsecase;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chat.config.websocket.WebSocketSessionManager;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class MessageDeliveryCoordinator {
    private final WebSocketSessionManager sessionManager;
    private final IMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    public MessageDeliveryCoordinator(
            WebSocketSessionManager sessionManager,
            IMessageRepository messageRepository,
            @Lazy SimpMessagingTemplate messagingTemplate) {
        this.sessionManager = sessionManager;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }
    
    public void deliverPendingMessage(UserId userId) {
        List<Message> pendingMessages = messageRepository.findPendingMessagesByReceiverId(userId);
        for (Message message : pendingMessages) {
            try {
                if (sessionManager.isUserOnline(userId.asString())) {
                    messagingTemplate.convertAndSendToUser(
                        message.getReceiverId().asString(),
                        "/queue/messages",
                        message
                    );
                    message.setStatus(MessageStatus.SENT);
                    messageRepository.save(message);
                }
            } catch (Exception e) {
                log.error("Failed to send pending message {}: {}", message.getId(), e.getMessage());
            }
        }
    }
}
