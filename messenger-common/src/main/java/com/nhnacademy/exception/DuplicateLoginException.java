package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;

public class DuplicateLoginException extends MessengerException {
    public DuplicateLoginException(String roomId) {
        super("존재하지 않는 방입니다: " + roomId, MessageType.ERROR);
    }
}
