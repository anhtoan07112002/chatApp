package com.chat.infrastructure.messaging;


import org.springframework.stereotype.Component;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.service.messageservice.IMessageListener;
import com.chat.domain.service.messageservice.IMessageSender;
import com.chat.domain.service.userservice.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageListenerImpl implements IMessageListener {
    
    private final IMessageRepository messageRepository;
    private final IMessageSender messageSender;
    private final IUserService userService;

    @Override
    public void onMessageReceived(Message message) {
        try {
            log.info("Received message: {}", message.getId());
            
            // Kiểm tra trạng thái người nhận
            if (userService.isOnline(message.getReceiverId().toString())) {
                // Nếu online thì gửi tin nhắn
                messageSender.sendMessage(message);
                message.setStatus(MessageStatus.SENT);
            } else {
                // Nếu offline thì đánh dấu là pending
                message.setStatus(MessageStatus.PENDING);
                log.info("Receiver is offline, message marked as pending");
            }
            
            // Lưu trạng thái mới của tin nhắn
            messageRepository.save(message);
            
        } catch (Exception e) {
            log.error("Error processing received message: {}", e.getMessage(), e);
            message.setStatus(MessageStatus.FAILED);
            messageRepository.save(message);
        }
    }
}