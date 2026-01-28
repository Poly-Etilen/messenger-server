package com.nhnacademy.domain.Header;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageHeader {
    private MessageType messageType;
    private LocalDateTime timestamp;

    MessageHeader(MessageType messageType, LocalDateTime timestamp){
        this.messageType = messageType;
        this.timestamp = timestamp;
    }


}
