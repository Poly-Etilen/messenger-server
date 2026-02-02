package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;

public class DuplicateLoginException extends MessengerException {
    public DuplicateLoginException(String userId) {
        super("이미 로그인된 사용자입니다: " + userId, MessageType.LOGIN_FAIL);
    }
}
