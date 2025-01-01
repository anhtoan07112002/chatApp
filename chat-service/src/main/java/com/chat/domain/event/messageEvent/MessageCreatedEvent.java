package com.chat.domain.event.messageEvent;

import com.chat.domain.entity.messages.Message;

import lombok.Getter;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class MessageCreatedEvent {
    @Getter
    @NonNull
    private final Message message;
}
