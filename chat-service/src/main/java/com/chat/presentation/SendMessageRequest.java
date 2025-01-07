package com.chat.presentation;

// import java.util.UUID;

import com.chat.domain.entity.user.UserId;

// import com.chat.domain.entity.messages.MessageContent;
// import com.chat.domain.entity.user.UserId;

import lombok.Data;

@Data
public class SendMessageRequest {
    private UserId senderId;
    private UserId receiverId;
    private String content;
}
