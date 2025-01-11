package com.chat.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.chat.application.DTO.SendMessageInput;
import com.chat.application.messageUsecase.SendMessageUseCase;
import com.chat.domain.entity.messages.Message;

@Controller
public class webSocketController {
    
    private final SendMessageUseCase sendMessageUseCase;

    public webSocketController(SendMessageUseCase sendMessageUseCase) {
        this.sendMessageUseCase = sendMessageUseCase;
    }

    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public void handleMessage(Message message) {
        SendMessageInput input = SendMessageInput.builder()
            .senderId(message.getSenderId().asString())
            .receiverId(message.getReceiverId().asString())
            .content(message.getContent().toString())
            .build();

        sendMessageUseCase.execute(input);
    }
}
