package com.chat.application.DTO;

// import java.util.UUID;

// import com.chat.domain.entity.messages.MessageId;
// import com.chat.domain.entity.user.UserId;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class MessageResponse {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String content;
    private String status;
    private String timestamp;
}
