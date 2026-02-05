package com.nhnacademy.messenger.client.event.handler;

import com.nhnacademy.domain.Message;

public interface Handler {
    void handler(Message message);
}
