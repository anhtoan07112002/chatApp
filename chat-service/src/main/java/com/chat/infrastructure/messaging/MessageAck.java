package com.chat.infrastructure.messaging;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageAck {
    private MessageId messageId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageId {
        private String vaUuid;
    }
}