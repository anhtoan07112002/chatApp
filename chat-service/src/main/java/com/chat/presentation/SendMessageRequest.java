package com.chat.presentation;

import java.util.UUID;

// import com.chat.domain.entity.messages.MessageContent;
// import com.chat.domain.entity.user.UserId;

import lombok.Data;

@Data
public class SendMessageRequest {
    private UUID senderId;
    private UUID receiverId;
    private String content;
}
