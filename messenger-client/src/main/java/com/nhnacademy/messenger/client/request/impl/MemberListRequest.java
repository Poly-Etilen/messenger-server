package com.nhnacademy.messenger.client.request.impl;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.messenger.client.request.Request;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class MemberListRequest implements Request {
    @Override
    public Message makeMessage() {
        MessageHeader header = new MessageHeader(MessageType.USER_LIST, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        return  new Message(header,payload);
    }
}
