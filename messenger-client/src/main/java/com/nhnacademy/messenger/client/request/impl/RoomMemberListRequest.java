package com.nhnacademy.messenger.client.request.impl;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.messenger.client.request.Request;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class RoomMemberListRequest implements Request {
    String roomId;
    @Override
    public Message makeMessage() {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_USER_LIST, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", this.roomId);

        return new Message(header,payload);

    }
}
