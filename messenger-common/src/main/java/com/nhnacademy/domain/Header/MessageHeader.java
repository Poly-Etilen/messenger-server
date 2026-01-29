package com.nhnacademy.domain.Header;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageHeader {
    private MessageType messageType;
    private LocalDateTime timestamp;

}
