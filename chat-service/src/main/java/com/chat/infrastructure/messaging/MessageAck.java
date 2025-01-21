package com.chat.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageAck {
    private String messageId;
}