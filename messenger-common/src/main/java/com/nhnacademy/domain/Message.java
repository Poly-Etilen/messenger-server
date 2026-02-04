package com.nhnacademy.domain;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.payload.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    MessageHeader header;
    MessagePayload payload;

}
