package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;

public class UserNotFoundException extends MessengerException {
    public UserNotFoundException(String userId) {
        super("사용자를 찾을 수 없습니다: " + userId, MessageType.ERROR);
    }
}
