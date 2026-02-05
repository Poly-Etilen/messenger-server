package com.nhnacademy.request.impl;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.request.Request;

import java.time.LocalDateTime;

public class RoomListRequest implements Request {
    @Override
    public Message makeMessage() {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_LIST, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        return new Message(header, payload);
    }
}
