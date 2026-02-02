package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;

public class InvalidRequestException extends MessengerException {
    public InvalidRequestException(String message) {
        super(message, MessageType.ERROR);
    }
}
