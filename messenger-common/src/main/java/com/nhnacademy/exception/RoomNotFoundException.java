package com.nhnacademy.exception;

import com.nhnacademy.domain.Header.MessageType;

public class RoomNotFoundException extends MessengerException {
    public RoomNotFoundException(String roomId) {
        super("존재하지 않는 방입니다: " + roomId, MessageType.ERROR);
    }
}
