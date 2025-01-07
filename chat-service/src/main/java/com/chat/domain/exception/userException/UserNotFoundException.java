package com.chat.domain.exception.userException;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final String errorDetail;

    public UserNotFoundException(String errorDetail) {
        super(errorDetail);
        this.errorDetail = errorDetail;
    }
}
