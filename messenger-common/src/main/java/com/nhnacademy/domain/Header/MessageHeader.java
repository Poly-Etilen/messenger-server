package com.nhnacademy.domain.Header;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageHeader {
    private MessageType messageType;
    private LocalDateTime timestamp;

}
