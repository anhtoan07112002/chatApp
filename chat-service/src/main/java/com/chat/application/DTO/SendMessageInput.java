package com.chat.application.DTO;

import com.chat.domain.entity.user.UserId;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendMessageInput {
    private UserId senderId;
    private UserId receiverId;
    private String content;
}
