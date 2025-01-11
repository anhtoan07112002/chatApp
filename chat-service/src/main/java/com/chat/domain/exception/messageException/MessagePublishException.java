package com.chat.domain.exception.messageException;

import lombok.Getter;

@Getter
public class MessagePublishException extends RuntimeException {
    private final String errorDetail;

    public MessagePublishException(String errorDetail, Exception exception) {
        super(errorDetail, exception);
        this.errorDetail = errorDetail;
    }
}

