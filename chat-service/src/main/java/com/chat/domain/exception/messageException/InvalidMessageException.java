package com.chat.domain.exception.messageException;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvalidMessageException {
    String errorDetail;
    Exception exception;
}
