package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;
import lombok.Getter;

@Getter
public class MessengerException extends RuntimeException {
    private final MessageType errorType;

    public MessengerException(String message, MessageType errorType) {
        super(message);
        this.errorType = errorType;
    }
}
