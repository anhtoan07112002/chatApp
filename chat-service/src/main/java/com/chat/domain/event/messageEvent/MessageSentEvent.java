package com.chat.domain.event.messageEvent;

import com.chat.domain.entity.messages.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class MessageSentEvent {
    @Getter
    @NonNull
    private final Message message;
}
