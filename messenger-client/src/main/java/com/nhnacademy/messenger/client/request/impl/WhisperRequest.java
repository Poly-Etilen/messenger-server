package com.nhnacademy.messenger.client.request.impl;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.messenger.client.request.Request;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class WhisperRequest implements Request {
    String targetId;
    String trim;

    @Override
    public Message makeMessage() {
        MessageHeader header = new MessageHeader(MessageType.PRIVATE_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("receiverId", targetId);
        payload.getData().put("message", trim);

        return new Message(header, payload);
    }
}
