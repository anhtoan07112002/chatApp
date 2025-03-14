package com.chat.presentation;

import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.service.messageservice.IMessageSender;
import com.chat.infrastructure.messaging.ErrorResponse;
import com.chat.infrastructure.messaging.MessageAck;
import com.chat.infrastructure.messaging.WebSocketMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.chat.application.DTO.SendMessageInput;
import com.chat.application.messageUsecase.SendMessageUseCase;
import com.chat.domain.entity.messages.Message;



import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Controller
@Slf4j
public class WebSocketController {
    private final SendMessageUseCase sendMessageUseCase;
    private final SimpMessagingTemplate template;
    private final WebSocketMessageSender webSocketMessageSender;

    public WebSocketController(
            SendMessageUseCase sendMessageUseCase,
            SimpMessagingTemplate template,
            WebSocketMessageSender webSocketMessageSender) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.template = template;
        this.webSocketMessageSender = webSocketMessageSender;
    }

    @MessageMapping("/chat.send")
    public void handleMessage(@Payload SendMessageRequest messageRequest) {
        try {
            log.debug("Received message request: {}", messageRequest);

            SendMessageInput input = SendMessageInput.builder()
                    .senderId(messageRequest.getSenderId())
                    .receiverId(messageRequest.getReceiverId())
                    .content(messageRequest.getContent())
                    .build();

            sendMessageUseCase.execute(input);
            log.debug("Message processed successfully");
        } catch (Exception e) {
            log.error("Error handling message", e);
            template.convertAndSendToUser(
                    messageRequest.getSenderId(),
                    "/queue/errors",
                    new ErrorResponse(UUID.randomUUID().toString(), e.getMessage())
            );
            throw e;
        }
    }

    @MessageMapping("/chat.ack")
    public void handleAcknowledgment(@Payload MessageAck ack) {
        log.debug("Received acknowledgment for message: {}", ack.getMessageId().getVaUuid());
        webSocketMessageSender.handleAcknowledgment(ack.getMessageId().getVaUuid());
    }
}