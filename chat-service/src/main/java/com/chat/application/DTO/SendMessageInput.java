package com.chat.application.DTO;


// import com.chat.domain.entity.user.UserId;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendMessageInput {
    private String senderId;
    private String receiverId;
    private String content;
}
