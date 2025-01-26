package com.chat.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // Thêm constructor không tham số
public class MessageAck {
    private String messageId;

    // Thêm JsonCreator để xử lý JSON từ client
    @JsonCreator
    public MessageAck(@JsonProperty("messageId") String messageId) {
        this.messageId = messageId;
    }
}