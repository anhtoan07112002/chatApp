package com.chat.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageContent;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.service.messageservice.IMessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final IMessageService messageService;

    public MessageController(IMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageRequest request) {
        UserId senderId = new UserId(request.getSenderId());
        UserId receiverId = new UserId(request.getReceiverId());
        MessageContent content = new MessageContent(request.getContent());

        Message message = messageService.sendMessage(senderId, receiverId, content);

        return ResponseEntity.ok(message);
    }
}