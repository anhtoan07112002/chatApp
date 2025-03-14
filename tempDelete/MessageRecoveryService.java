//package com.chat.application.messageUsecase;
//
//import com.chat.domain.entity.messages.Message;
//import com.chat.domain.entity.messages.MessageStatus;
//import com.chat.domain.repository.messageReponsitory.IMessageRepository;
//import com.chat.domain.service.userservice.IUserService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class MessageRecoveryService {
//
//    private final MessageDeliveryCoordinator deliveryCoordinator;
//    private final IMessageRepository messageRepository;
//    private final IUserService userService;
//
//    public MessageRecoveryService(
//            MessageDeliveryCoordinator deliveryCoordinator,
//            IMessageRepository messageRepository,
//            IUserService userService) {
//        this.deliveryCoordinator = deliveryCoordinator;
//        this.messageRepository = messageRepository;
//        this.userService = userService;
//    }
//
//    @KafkaListener(topics = "message-topic", groupId = "message-recovery-group")
//    public void processMessage(Message message) {
//        try {
//            // Check if message is already delivered
//            Message existingMessage = messageRepository.findById(message.getId());
//            if (existingMessage != null && existingMessage.getStatus() == MessageStatus.SENT) {
//                return; // Message already delivered
//            }
//
//            // Try to deliver if recipient is online
//            if (userService.isOnline(message.getReceiverId().asString())) {
//                try {
//                    deliveryCoordinator.sendMessage(message);
//                    message.setStatus(MessageStatus.SENT);
//                    messageRepository.save(message);
//                } catch (Exception e) {
//                    log.warn("Failed to deliver recovered message: {}", e.getMessage());
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error processing recovered message: {}", e.getMessage());
//        }
//    }
//}
