package com.chat.infrastructure.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.service.messageservice.IMessageSender;

@Component
public class WebSocketMessageSender implements IMessageSender {
    private final SimpMessagingTemplate template;
    
    public WebSocketMessageSender(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Override
    public void sendMessage(Message message) {
        // send message to websocket
        template.convertAndSendToUser(message.getReceiverId().toString(), "/queue/messages", message.getContent());
    }
    
}
