package com.chat.application.DTO;

import com.chat.domain.entity.user.UserId;

import lombok.Data;

@Data
public class SendMessageInput {
    private UserId senderId;
    private UserId receiverId;
    private String content;
}
