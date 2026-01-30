package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;

public class NotAuthorizedException extends MessengerException{
    public NotAuthorizedException() {
        super("로그인이 필요한 서비스입니다.", MessageType.LOGIN_FAIL);
    }
}
