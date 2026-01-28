package com.nhnacademy.domain;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.payload.MessagePayload;


public class Message {
    String lengthLine;
    MessageHeader header;
    MessagePayload payload;

    Message(String lengthLine, MessageHeader header, MessagePayload payload){
        this.lengthLine = lengthLine;
        this.header = header;
        this.payload = payload;
    }

}
