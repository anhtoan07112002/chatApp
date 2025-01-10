package com.chat.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.application.DTO.MessageResponse;
import com.chat.application.DTO.SendMessageInput;
import com.chat.application.messageUsecase.SendMessageUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final SendMessageUseCase sendMessageUseCase;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        SendMessageInput input = SendMessageInput.builder()
            .senderId(request.getSenderId())
            .receiverId(request.getReceiverId())
            .content(request.getContent())
            .build();

        sendMessageUseCase.execute(input);
        
        // Build response
        MessageResponse response = MessageResponse.builder()
            .senderId(request.getSenderId())
            .receiverId(request.getReceiverId())
            .content(request.getContent())
            .status("SENT")
            .timestamp(java.time.LocalDateTime.now().toString())
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<String> getMessage() {
        return ResponseEntity.ok("Hello World");
    }
}