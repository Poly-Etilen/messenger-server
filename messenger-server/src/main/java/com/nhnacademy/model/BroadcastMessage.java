package com.nhnacademy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BroadcastMessage {
    private final ChatRoom chatRoom;
    private final String senderId;
    private final String content;
    private final long messageId;
}
