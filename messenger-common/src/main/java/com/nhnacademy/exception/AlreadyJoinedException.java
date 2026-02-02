package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;

public class AlreadyJoinedException extends MessengerException {
    public AlreadyJoinedException() {
        super("이미 참여 중인 방입니다.", MessageType.ERROR);
    }
}
