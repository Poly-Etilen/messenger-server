package com.nhnacademy.messenger.client.request.impl;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.messenger.client.request.Request;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class CreateRoomRequest implements Request {
    String roomName;

    @Override
    public Message makeMessage() {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_CREATE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomName", roomName);

        return new Message(header, payload);

    }
}
