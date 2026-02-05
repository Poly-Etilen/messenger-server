package com.nhnacademy.request.impl;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.request.Request;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class BroadCastRequest implements Request {
    String roomId;
    String myUserId;
    String message;

    @Override
    public Message makeMessage() {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", roomId);
        payload.getData().put("senderId", myUserId);
        payload.getData().put("message", message);

        return new Message(header,payload);
    }
}
