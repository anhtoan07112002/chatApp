package com.chat.domain.entity.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Thêm constructor không tham số
public class MessageContent {
    private String content;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MessageContent(@JsonProperty("content") String content) {
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }
}