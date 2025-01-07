package com.chat.domain.exception.messageException;

import lombok.Getter;

@Getter
public class MessageProcessingException extends RuntimeException {
    private final String errorDetail;

    public MessageProcessingException(String errorDetail, Exception exception) {
        super(errorDetail, exception);
        this.errorDetail = errorDetail;
    }
}
